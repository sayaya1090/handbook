package dev.sayaya.handbook.entity.attributes

import dev.sayaya.handbook.entity.Attribute
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("File")
internal class FileAttribute: Attribute() {
    @Column(name="file_extensions", columnDefinition = "text") var fileExtensions: String? = null
}