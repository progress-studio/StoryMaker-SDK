package kr.progress.story.parser.story

import kr.progress.story.parser.XMLBody
import kr.progress.story.parser.XMLDecodable
import kr.progress.story.parser.XMLNode

data class StringVariable(
    val id: String,
    val body: Body
) : Variable() {
    companion object : XMLDecodable<StringVariable> {
        override fun invoke(node: XMLNode): StringVariable {
            return StringVariable(
                id = node.attributes["id"]!!,
                body = Body(node)
            )
        }
    }

    override fun toXMLNode(): XMLNode {
        return XMLNode(
            tag = "string",
            attributes = mapOf("id" to id) + when (body) {
                is Body.Conditional -> body.value.associate {
                    when (it) {
                        is Body.Conditional.Equals -> "equals" to it.operand
                        is Body.Conditional.StartsWith -> "startswith" to it.operand
                        is Body.Conditional.EndsWith -> "endswith" to it.operand
                    }
                }

                is Body.SetValue -> mapOf(
                    "set" to body.value
                )
            },
            body = if (body is Body.Conditional) body.condition.toChildren() else null
        )
    }

    sealed class Body {
        companion object : XMLDecodable<Body> {
            override fun invoke(node: XMLNode): Body {
                return when (node.body) {
                    is XMLBody.Children -> Conditional(node)
                    is XMLBody.Value -> throw IllegalStateException()
                    null -> SetValue(node.attributes["set"]!!)
                }
            }
        }

        data class Conditional(
            val value: Set<Value>,
            val condition: Condition
        ) : Body() {
            companion object : XMLDecodable<Conditional> {
                override fun invoke(node: XMLNode): Conditional {
                    return Conditional(
                        value = node.attributes.mapNotNull { (key, value) ->
                            when (key) {
                                "equals" -> Equals(value)
                                "startswith" -> StartsWith(value)
                                "endswith" -> EndsWith(value)
                                else -> null
                            }
                        }.toSet(),
                        condition = Condition(node.childrenToMap())
                    )
                }
            }

            sealed class Value
            data class Equals(val operand: String) : Value()
            data class StartsWith(val operand: String) : Value()
            data class EndsWith(val operand: String) : Value()
        }

        data class SetValue(
            val value: String
        ) : Body()
    }
}