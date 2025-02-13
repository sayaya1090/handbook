package dev.sayaya.handbook.entity.attributes

import dev.sayaya.handbook.entity.Attribute
import dev.sayaya.handbook.entity.Type
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("File")
internal class FileAttribute: Attribute() {
    @Column(name="file_extensions", columnDefinition = "text") var fileExtensions: String? = null
    companion object {
        fun of(type: Type, name: String, fileExtensions: String? = null) = FileAttribute().apply {
            this.type = type
            this.name = name
            this.fileExtensions = fileExtensions
        }
    }
}