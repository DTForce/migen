package com.dtforce.migen.test.mock2kt.entities

import com.dtforce.migen.adapter.hibernate.type.OtherStringType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import lombok.Data
import org.hibernate.annotations.Comment
import org.hibernate.annotations.Type

/**
 * Client entity.
 */
@Entity
@Data
class Client {
    @Id
    private val id: String? = null

    /**
     * Authorities setting the content of the JWT claim `authorities` for the given [Client].
     */
    @ElementCollection
    @Comment("test")
    private val authorities: Set<String>? = null

    /**
     * This is a path of the [Client].
     *
     * Required field.
     */
    @Column(name = "path", nullable = false, unique = true, columnDefinition = "ltree")
    @Type(value = OtherStringType::class)
    private var path: String? = null

    @Column(name = "description", columnDefinition = "text")
    @Comment("This is a description of the field description.")
    private var description: String? = null
}
