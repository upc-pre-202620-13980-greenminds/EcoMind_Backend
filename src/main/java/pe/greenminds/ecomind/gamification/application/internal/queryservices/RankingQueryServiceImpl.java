package pe.greenminds.ecomind.gamification.application.internal.queryservices;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.gamification.application.queryservices.RankingQueryService;
import pe.greenminds.ecomind.gamification.application.outboundservices.RankingParticipantDirectory;
import pe.greenminds.ecomind.gamification.domain.repositories.RankingReadRepository;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPeriod;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingEntry;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingTransaction;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingParticipant;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPage;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

@Service
@Transactional(readOnly = true)
public class RankingQueryServiceImpl implements RankingQueryService {
  private final RankingParticipantDirectory directory;
  private final RankingReadRepository rankings;

  public RankingQueryServiceImpl(RankingParticipantDirectory directory, RankingReadRepository rankings) {
    this.directory = directory;
    this.rankings = rankings;
  }

  public List<RankingType> types() {
    return List.of(RankingType.GLOBAL, RankingType.FRIENDS, RankingType.FAMILIES);
  }

  public RankingPage<RankingEntry> participants(RankingType type, UserId requester, int page, int size) {
    int offset = validate(type, page, size);
    var participants = directory.participants(type, requester).stream()
        .sorted(Comparator.comparing(RankingParticipant::id)).toList();
    if (offset >= participants.size()) return new RankingPage<>(List.of(), page, size, false);
    int end = (int) Math.min((long) offset + size, participants.size());
    var selected = participants.subList(offset, end);
    var scores = rankings.totals(type, selected.stream().map(RankingParticipant::id).toList());
    var entries = selected.stream().map(participant -> new RankingEntry(participant.id(),
        participant.displayName(), scores.getOrDefault(participant.id(), 0L))).toList();
    return new RankingPage<>(entries, page, size, end < participants.size());
  }

  public RankingPage<RankingTransaction> transactions(RankingType type, UserId requester,
      RankingPeriod period, int page, int size) {
    int offset = validate(type, page, size);
    var ids = directory.participants(type, requester).stream().map(RankingParticipant::id).toList();
    var entries = rankings.transactions(type, ids, period, offset, size + 1);
    boolean hasNext = entries.size() > size;
    return new RankingPage<>(hasNext ? entries.subList(0, size) : entries, page, size, hasNext);
  }

  private int validate(RankingType type, int page, int size) {
    if (type == null || !types().contains(type))
      throw new IllegalArgumentException("Unsupported ranking type");
    if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE)
      throw new IllegalArgumentException("Invalid pagination; size must be between 1 and 100");
    return page * size;
  }
}
