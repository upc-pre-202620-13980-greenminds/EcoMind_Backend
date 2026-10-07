package pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.util.Date;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Base class for persistence entities whose table has the created_at and updated_at columns.
 *
 * <p>It does not declare the identifier: each entity maps its own id as defined in the database
 * design of its bounded context.</p>
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableAbstractPersistenceEntity {

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Date createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private Date updatedAt;
}
