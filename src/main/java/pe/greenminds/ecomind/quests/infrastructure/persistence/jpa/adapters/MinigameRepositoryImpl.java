package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Minigame;
import pe.greenminds.ecomind.quests.domain.repositories.MinigameRepository;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.assemblers.MinigamePersistenceAssembler;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.repositories.MinigamePersistenceRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class MinigameRepositoryImpl implements MinigameRepository {
    private final MinigamePersistenceRepository minigamePersistenceRepository;

    public MinigameRepositoryImpl(MinigamePersistenceRepository minigamePersistenceRepository) {
        this.minigamePersistenceRepository = minigamePersistenceRepository;
    }

    @Override
    public List<Minigame> findAll() {
        return minigamePersistenceRepository.findAll()
                .stream()
                .map(MinigamePersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Optional<Minigame> findById(Long id) {
        return minigamePersistenceRepository.findById(id)
                .map(MinigamePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public boolean existsById(Long id) {
        return minigamePersistenceRepository.existsById(id);
    }

    @Override
    public Minigame save(Minigame minigame) {
        return MinigamePersistenceAssembler.toDomainFromPersistence(
                minigamePersistenceRepository.save(
                        MinigamePersistenceAssembler.toPersistenceFromDomain(minigame)
                )
        );
    }

    @Override
    public void deleteById(Long id) {
        minigamePersistenceRepository.deleteById(id);
    }
}
