package kr.progress.story.format.project

import kr.progress.story.parser.XMLDecodable
import kr.progress.story.parser.XMLNode

data class BooleanVariable(
    override val id: String,
    val name: String,
    val default: Boolean?
) : Variable() {
    companion object : XMLDecodable<BooleanVariable> {
        override operator fun invoke(node: XMLNode) = BooleanVariable(
            id = node.attributes["id"]!!,
            name = node.attributes["name"]!!,
            default = node.attributes["default"]?.toBoolean()
        )
    }

    override fun toXMLNode() = XMLNode(
        tag = "boolean",
        attributes = mutableMapOf(
            "id" to id,
            "name" to name
        ).apply {
            default?.toString()?.let {
                this["default"] = it
            }
        }
    )
}