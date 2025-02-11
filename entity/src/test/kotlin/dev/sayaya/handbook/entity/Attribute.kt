package dev.sayaya.handbook.entity

import jakarta.persistence.*

@Embeddable
internal class Attribute {
    @Column(nullable = false) lateinit var name: String
    @Column(nullable = false) lateinit var type: AttributeType
    @Column(nullable = false) var primitive: Boolean = false
    @Column(nullable = false) var nullable: Boolean = false
    @Column var regex: String? = null                   // type = value 일 때만 유효
    @Column var subtype: Attribute? = null              // type = array, enum, enumset, map 일 때만 유효
    @Column var valueSubtype: Attribute? = null         // type = map 일 때만 유효
    @Column var referenceType: AttributeType? = null    // type = document일 때만 유효
    @Column var fileExtensions: String? = null          // type = file일 때만 유효 
    @Column var description: String? = null
}