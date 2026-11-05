package pe.greenminds.ecomind.learning.application;

import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.learning.domain.model.MaterialCategory;
import pe.greenminds.ecomind.learning.domain.model.MaterialType;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.entities.EducationalMaterialEntity;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.repositories.EducationalMaterialRepository;

@Component
public class LearningCatalogSeed implements ApplicationRunner {
  private final EducationalMaterialRepository materials;
  public LearningCatalogSeed(EducationalMaterialRepository materials) { this.materials = materials; }

  @Override
  public void run(ApplicationArguments args) {
    if (materials.count() > 0) return;
    materials.saveAll(List.of(
        material("Reciclaje esencial", "Aprende a separar correctamente tus residuos.",
            "Reciclar reduce el uso de materias primas y evita que residuos aprovechables lleguen a vertederos.\n\n1. Separa papel y cartón.\n2. Enjuaga envases.\n3. Identifica los puntos de reciclaje de tu comunidad.",
            MaterialType.TEXT, MaterialCategory.RECYCLE,
            "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?w=900", null, 7, true),
        material("Energía verde", "Conoce las principales fuentes de energía renovable.",
            "La energía solar, eólica e hidroeléctrica se obtiene de fuentes que se regeneran naturalmente y producen menos emisiones que los combustibles fósiles.",
            MaterialType.VIDEO, MaterialCategory.ENERGY,
            "https://images.unsplash.com/photo-1473341304170-971dccb5ac1e?w=900",
            "https://www.youtube.com/embed/1kUE0BZtTRc", 5, false),
        material("Cambio climático", "Comprende sus causas, efectos y soluciones.",
            "Las actividades humanas han elevado la concentración de gases de efecto invernadero. Ahorrar energía, reducir residuos y elegir transporte sostenible ayuda a disminuir el impacto.",
            MaterialType.TEXT, MaterialCategory.ENVIRONMENT,
            "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=900", null, 7, true),
        material("Compostaje en casa", "Convierte residuos orgánicos en abono.",
            "Combina materiales verdes, como restos de frutas, con materiales marrones, como hojas secas. Mantén la mezcla húmeda y remuévela regularmente.",
            MaterialType.TEXT, MaterialCategory.RECYCLE,
            "https://images.unsplash.com/photo-1585336261022-680e295ce3fe?w=900", null, 7, true),
        material("Tratamiento del agua", "Una infografía sobre el recorrido del agua.",
            "El agua pasa por captación, filtración, sedimentación y desinfección antes de llegar de forma segura a nuestros hogares.",
            MaterialType.INFOGRAPHIC, MaterialCategory.WATER,
            "https://images.unsplash.com/photo-1504384308090-c894fdcc538d?w=900", null, 4, true),
        material("Biodiversidad y tú", "Descubre cómo proteger las especies de tu comunidad.",
            "Plantar especies nativas, evitar pesticidas y conservar espacios verdes protege la variedad de seres vivos y los servicios que brindan los ecosistemas.",
            MaterialType.INFOGRAPHIC, MaterialCategory.ENVIRONMENT,
            "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=900", null, 6, true)
    ));
  }

  private EducationalMaterialEntity material(String title, String description, String content,
      MaterialType type, MaterialCategory category, String image, String video, int minutes,
      boolean downloadable) {
    var entity = new EducationalMaterialEntity();
    entity.setTitle(title); entity.setDescription(description); entity.setContent(content);
    entity.setMaterialType(type); entity.setCategory(category); entity.setImageUrl(image);
    entity.setVideoUrl(video); entity.setDurationMinutes(minutes); entity.setLanguage("es");
    entity.setDownloadable(downloadable); entity.setActive(true);
    return entity;
  }
}
