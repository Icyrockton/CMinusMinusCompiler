package compiler

import compiler.Token.Companion.isDelimiter
import compiler.Token.Companion.isKeyWord
import compiler.Token.Companion.isLegalIdentifier
import compiler.Token.Companion.isLegalNumber
import compiler.Token.Companion.isOperator

/**
 *@Author : Icyrockton
 *@Date: 2021/6/2 20:14
 * 词法分析器
 **/
class LexAnalyzer() {

    fun parse(input: String): List<Token> {
        val lines = input.lines()//所有的行
        val tokens = mutableListOf<Token>()
        var currentLine = 1
        lines.forEach { line ->
            tokens.addAll(parseLine(line, currentLine++))
        }
        return tokens
    }

    private var isComment: Boolean = false //注释
    private var commentBuffer = ""

    private fun parseLine(line: String, lineNumber: Int): List<Token> {
        val tokens = mutableListOf<Token>()
        var cursor = 0 //当前的游标
        var startIndex = 0
        var endIndex = 0
        var needStepBack = false
        while (cursor < line.length) {
            val char = line[cursor]
            needStepBack = false
            if (!isComment) {
                if (char.isLetter() || char == '_') {
                    startIndex = cursor
                    cursor++
                    while (cursor < line.length) {
                        //换行符
                        if (line[cursor] == '\n' || line[cursor] == '\t' || line[cursor] == '\r' || line[cursor] == ' ')
                            break
                        //界符
                        if (line[cursor].isDelimiter() || line[cursor].isOperator()) {
                            needStepBack = true
                            break
                        }
                        cursor++
                    }
                    endIndex = cursor
                    val tokenString = line.substring(startIndex, endIndex)

                    //不可能是[Operator]或者[Delimiter]
                    if (tokenString.isKeyWord()) { //是关键字
                        //操作符
                        val token = Token(TokenType.KeyWord, lineNumber, tokenString)
                        tokens.add(token)

                    } else if (tokenString.isLegalIdentifier()) { //是标识符
                        val token = Token(TokenType.Identifier, lineNumber, tokenString)
                        tokens.add(token)
                    } else {
                        throw UnresolvedIdentifier("无法解析的字符串:${tokenString}", lineNumber)
                    }
                } else if (char.isDigit()) { //数字
                    startIndex = cursor
                    cursor++
                    while (cursor < line.length) {

                        //换行符
                        if (line[cursor] == '\n' || line[cursor] == '\t' || line[cursor] == '\r' || line[cursor] == ' ')
                            break
                        //界符
                        if (line[cursor].isDelimiter() || line[cursor].isOperator()) {
                            needStepBack = true
                            break
                        }

                        if (line[cursor].isDigit()) {
                            cursor++
                        } else {
                            throw UnresolvedNumber("解析数字失败${line.substring(startIndex, cursor)}", lineNumber)
                        }
                    }
                    endIndex = cursor

                    val numberString = line.substring(startIndex, endIndex)

                    if (numberString.isLegalNumber()) {
                        //Int常量
                        val token = Token(TokenType.IntConstant, lineNumber, numberString)
                        tokens.add(token)
                    } else {
                        throw UnresolvedNumber("解析数字失败${numberString}", lineNumber)
                    }

                } else if (char == '"') {  //string常量
                    startIndex = cursor
                    cursor++
                    while (cursor < line.length) {
                        if (line[cursor] == '"')
                            break
                        cursor++
                    }
                    if (cursor >= line.length && line.last() != '"') {
                        throw UnresolvedString("无法解析的String:${line.substring(startIndex)}", lineNumber)
                    }
                    endIndex = cursor
                    val string = line.substring(startIndex + 1, endIndex)
                    tokens.add(Token(TokenType.Delimiter, lineNumber, "\""))
                    tokens.add(Token(TokenType.StringConstant, lineNumber, string))
                    tokens.add(Token(TokenType.Delimiter, lineNumber, "\""))
                    cursor++ //跳过最后一个"
                } else if (char == '=' || char == '+' || char == '-' || char == '*' || char == '/' || char == '%' || char == '!') {
                    cursor++;
                    if (line[cursor] == '=') {  // == , += , -= , *= , /+ , %= , !=
                        tokens.add(Token(TokenType.Operator, lineNumber, "${char}="))
                    } else if ((line[cursor] == '+' && char == '+') || (line[cursor] == '-' && char == '-')) {
                        // ++ --
                        tokens.add(Token(TokenType.Operator, lineNumber, "${char}${line[cursor]}"))
                    } else if (line[cursor] == '/' && char == '/') {
                        //  单行注释 //xxxx
                        cursor++
                        //吃完本行的所有注释
                        commentBuffer = line.substring(cursor)
                        tokens.add(Token(TokenType.Comment,lineNumber,commentBuffer))
                        cursor = line.length

                    } else if (char == '/' && line[cursor] == '*') {
                        //多行注释 /* xxxx  */
                        isComment = true
                        cursor++
                        commentBuffer = line.substring(cursor)
                        cursor = line.length
                    } else { // =
                        cursor--   //回退
                        tokens.add(Token(TokenType.Operator, lineNumber, "$char"))
                    }
                } else if (char == '>' || char == '<') {
                    // >, < , >= , <=
                    cursor++
                    if (line[cursor] == '=') { // >= , <=
                        tokens.add(Token(TokenType.Operator, lineNumber, "$char${line[cursor]}"))
                    } else {
                        cursor--
                        tokens.add(Token(TokenType.Operator, lineNumber, "$char"))
                    }
                } else if (char == '&' || char == '|') {
                    // && ||   暂不支持位运算符
                    cursor++
                    if ((line[cursor] == '&' && char == '&') || (line[cursor] == '|' && char == '|')) {
                        tokens.add(Token(TokenType.Operator, lineNumber, "$char${line[cursor]}"))
                    } else {
                        throw UnresolvedOperator("未知的operator $char${line[cursor]}", lineNumber)
                    }
                } else if (char == '(' || char == ')' || char == '{' || char == '}' || char == ';' || char == ',') {
                    //分隔符
                    tokens.add(Token(TokenType.Delimiter, lineNumber, "${char}"))
                }


            } else{
                //多行注释
                if (char == '*'){
                    if (cursor < line.length - 1 && line[cursor + 1] == '/'){
                        //多行注释结束
                        cursor ++
                        isComment = false
                        tokens.add(Token(TokenType.Comment,lineNumber,commentBuffer))
                        commentBuffer = ""

                    }
                    else{
                        commentBuffer += char
                    }
                }
                else
                    commentBuffer += char
            }

            if (needStepBack == false) {
                cursor++
            }
        }
        if (isComment)
            commentBuffer += '\n' //换行

        return tokens
    }
}