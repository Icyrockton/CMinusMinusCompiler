package compiler

/**
 *@Author : Icyrockton
 *@Date: 2021/6/2 19:26
 * [type] token类型
 * [line] token所在的行数
 * [value] 值,可能为空
 **/
data class Token(
    val type: TokenType,
    val line: Int,
    val value: String? = null
) {
    fun print() {
        print("< ")
        when (type) {
            TokenType.Identifier -> print("Identifier")
            TokenType.KeyWord -> print("KeyWord")
            TokenType.IntConstant -> print("IntConstant")
            TokenType.StringConstant -> print("StringConstant")
            TokenType.Delimiter -> print("Delimiter")
            TokenType.Operator -> print("Operator")
            TokenType.Comment -> print("Comment")
        }
        print(" , ")
        print(line)
        if (value != null) {
            print(" , ")
            print(value)
        }
        println(" >")
    }

    companion object {
        val Operator: List<String> = arrayListOf<String>().apply {
            //算数运算符
            add("+");add("-");add("*");add("/")
            add("%");add("++");add("--")

            add("==");add("!=");add(">");add("<")
            add(">=");add("<=")

            //逻辑运算符
            add("&&");add("||");add("!")

            //赋值运算符
            add("=");add("+=");add("-=")
            add("*=");add("/=");add("%=")

            //暂不支持按位逻辑
        }
        val OperatorChar: List<Char> = arrayListOf<Char>().apply {
            add('+');add('-');add('*');add('/');
            add('%');add('&');add('|');add('!');
            add('=');
        }

        val Delimiter: List<Char> = arrayListOf<Char>().apply {
            add('(');add(')');add('{')
            add('}');add(';');add(',')
            add('"');
        }

        val KeyWord: List<String> = arrayListOf<String>().apply {
            add("int");add("string");add("bool");
            add("void"); add("if"); add("else");
            add("switch"); add("case"); add("for");
            add("while"); add("continue"); add("break");
            add("return"); add("printf");
            add("true"); add("false")
        }

        fun String.isOperator(): Boolean = Operator.contains(this)

        fun Char.isDelimiter(): Boolean = Delimiter.contains(this)

        fun Char.isOperator(): Boolean = OperatorChar.contains(this)

        fun String.isKeyWord(): Boolean {
            return KeyWord.contains(this)
        }

        fun Token.isVarType() : Boolean = (this.type == TokenType.KeyWord &&
                                        (this.value == "int" || this.value =="string" || this.value =="bool") || this.value == "void")

        fun Token.isComment () : Boolean = this.type == TokenType.Comment

        fun Token.isEqual () : Boolean = this.type == TokenType.Operator && this.value == "="

        fun Token.isID() : Boolean= this.type == TokenType.Identifier

        fun Token.isLogicalOP() : Boolean = (this.type == TokenType.Operator  &&
                (this.value == "&&" || this.value == "||" || this.value == "==" || this.value == "!=" || this.value == "<="
                        || this.value == ">=" || this.value =="<" || this.value ==">")

                )
        fun String.isLogicalOP() : Boolean = (this == "&&" || this == "||" || this == "==" || this == "!=" || this == "<="
                || this == ">=" || this =="<" || this ==">")

        fun String.isLegalIdentifier(): Boolean {
            if (length == 0)
                return false
            if (!(this[0].isLetter() || this[0] == '_'))
                return false
            for (i in 1 until this.length) {
                //c语言中标识符是由字母(A-Z,a-z)、数字(0-9)、下划线“_”组成,
                // 并且首字符不能是数字,但可以是字母或者下划线。例如,正确的标识符:abc,a1,prog_to
                if (!(this[i].isLetterOrDigit() || this[i] == '_'))
                    return false
            }
            return true
        }

        fun String.isLegalNumber() : Boolean{
            for (i in 0 until this.length)
                if (!this[i].isDigit())
                    return false
            return true
        }


        fun List<Token>.prettyPrint() {
            this.forEach {
                it.print()
            }
        }

    }

}

sealed class TokenType {
    object Operator : TokenType()
    object KeyWord : TokenType()
    object IntConstant : TokenType()
    object StringConstant : TokenType()
    object Delimiter : TokenType()
    object Identifier : TokenType()
    object Comment : TokenType()
}