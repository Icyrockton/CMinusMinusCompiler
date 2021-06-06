package compiler

/**
 *@Author : Icyrockton
 *@Date: 2021/6/2 22:27
 **/

/*
    词法分析器异常
 */
abstract class LexException(override val message: String, open val lineNumber: Int) : Exception(message)

/*
    无法解析的符号ID
 */
class UnresolvedIdentifier(override val message: String, override val lineNumber: Int) :
    LexException(message, lineNumber) {
    override fun toString(): String {
        return "解析ID异常  ${message}  行号${lineNumber}"
    }
}

/*
    无法解析的Int数字
 */
class UnresolvedNumber(override val message: String, override val lineNumber: Int) :
    LexException(message, lineNumber) {
    override fun toString(): String {
        return "解析Int异常  ${message}   行号${lineNumber}"
    }
}

class UnresolvedString(override val message: String, override val lineNumber: Int) :
    LexException(message, lineNumber) {
    override fun toString(): String {
        return "解析String异常  ${message}   行号${lineNumber}"
    }
}


class UnresolvedOperator(override val message: String, override val lineNumber: Int) :
    LexException(message, lineNumber) {
    override fun toString(): String {
        return "解析Operator异常    ${message}  行号${lineNumber}"
    }
}
