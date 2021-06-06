package compiler

import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

/**
 * @Author : Icyrockton
 * @Date: 2021/6/3 19:17
 */
class LLParserTest {

    private val lexAnalyzer = LexAnalyzer()
    private val llParser = LLParser()
    private fun parse(intput: String, printAnalysisInfo: Boolean = false, printGeneratedQuadruples: Boolean = false) {
        llParser.parse(lexAnalyzer.parse(intput), printAnalysisInfo, printGeneratedQuadruples)
    }

    private fun testIR(intput: String) {
        parse(intput, false, true)
    }

    @Test
    fun testMain() {
        parse(
            """
            int main()
            {
                return 0;
            }
        """.trimIndent()
        )
    }

    @Test
    fun testUnExpectedReturnType() {
        parse(
            """
            char main()
            {
                return 0;
            }
        """.trimIndent()
        )
    }

    @Test
    fun testUnExpectedVarType() {
        parse(
            """
           int main()
            {
                int a = 0 ;
                a=20;
                return 0;
            }
        """.trimIndent()
        )
    }


    @Test
    fun testMultiVar() {
        parse(
            """
           int main()
            {
                int a = 0 , b= 20 ;
                a=20;
                return 0;
            }
        """.trimIndent()
        )
    }

    @Test
    fun testAssign() {
        parse(
            """
            int main(){
            int a=10,b;;
            ;;;;;;;
                return 0;
            }
        """.trimIndent()
        )
    }

    @Test
    fun testMultiAssign() {
        parse(
            """
            int main(){
                int a = 10 , b = 20 , c = 100 , d=a ;
                return 0;
             }
        """.trimIndent()
        )
    }

    @Test
    fun testIf() {
        parse(
            """
            int main(){
                int a = 10 ;
                if ( a > 20 )
                    a= -5 ;
                return 0;
             }
        """.trimIndent()
        )
    }

    @Test
    fun testIfBracket() {
        parse(
            """
            int main(){
                int a = 10 ;
                if ( a > 20 ){
                    a= -5 ;
               }
                return 0;
             }
        """.trimIndent()
        )
    }

    @Test
    fun testElse() {
        parse(
            """
            int main(){
                int a = 10 ;
                if ( a > 20 )
                    a= -5 ;
               else 
                   a=10;
                return 0;
             }
        """.trimIndent()
        )
    }

    @Test
    fun testIfBracketElse() {
        parse(
            """
            int main(){
                int a = 10 ;
                int b = -20;
                if ( a > 20 ) {
                    a= -5 ;
                    b= 10;
                }
               else 
                   a=10;
                return 0;
             }
        """.trimIndent()
        )
    }

    @Test
    fun testIfBracketElseBracket() {
        parse(
            """
            int main(){
                int a = 10 ;
                int b = -20;
                if ( a > 20 ) {
                    a= -5 ;
                    b= 10;
                }
               else {
                   a=10;
                   b=100;
               }
                return 0;
             }
        """.trimIndent()
        )
    }

    @Test
    fun testIfElseIfElse() {
        parse(
            """
            int main(){
                int a = 10 ;
                int b = -20;
                if ( a > 20 ) {
                    a= -5 ;
                    b= 10;
                }
               else if ( a > 10 ){
                   a=10;
                   b=100;
               }
               else{
                    a=-10;
                    b=20;
               }
                return 0;
             }
        """.trimIndent()
        )
    }


    @Test
    fun testIfLogical() {
        parse(
            """
            int main(){
                int a = 10 ;
                int b = -20;
                if (  a >= 10  ) {
                    a = -5;
                }
                else if(a==0){ 
                    a=5;
                }
                else if(a <= 2){
                    a=-5;
                }
                else if ( a != 10){
                    a=-100;
                }
                else if( a >= 100){
                    a= 2020;
                }
                return 0;
             }
        """.trimIndent()
        )
    }

    @Test
    fun testFor() {
        parse(
            """
            int main(){
                int a ;
                int b ;
                for( a = 0 ; a < 100 ; a=a+1){
                   b = a;
                   if(b > 30){
                    b = 0;
                  }
                }
                return 0;
             }
        """.trimIndent()
        )
    }

    @Test
    fun testWhile() {
        parse(
            """
            int main () {
                int a=10;
                while( a > 0 ){
                    a = a-1;
                }
                return 0;
            }
        """.trimIndent()
        )
    }

    @Test
    fun testWhileBlank() {
        parse(
            """
            int main () {
                int a=10;
                while( a > 0 );
                return 0;
            }
        """.trimIndent()
        )
    }


    @Test
    fun testBlank() {
        parse(
            """
           
        """.trimIndent()
        )
    }


    @Test
    fun testNoReturnType() {
        parse(
            """
            main()
            {
                return 0;
            }
        """.trimIndent()
        )
    }

    @Test
    fun testNoFunctionName() {
        parse(
            """
            int ()
            {
                return 0;
            }
        """.trimIndent()
        )
    }

    @Test
    fun irAdd() = testArithmeticExpressionIR(
        """
            2+30+40+50+60
        """.trimIndent()
    )

    @Test
    fun irSub() = testArithmeticExpressionIR(
        """
            2-30-40-50-60
        """.trimIndent()
    )

    @Test
    fun irMul() = testArithmeticExpressionIR(
        """
            2 * 30 * 40 * 50 * 60
        """.trimIndent()
    )

    @Test
    fun irDiv() = testArithmeticExpressionIR(
        """
            2 / 30 / 40 / 50 / 60
        """.trimIndent()
    )

    @Test
    fun irMulAndDiv() = testArithmeticExpressionIR(
        """
            (2 * 30) / (20 / 10) 
        """.trimIndent()
    )

    @Test
    fun irAddAndSubAndMulAndDiv() = testArithmeticExpressionIR(
        """
            (2 * 30 + (60 + 70)) / (20 / 10) -  100 
        """.trimIndent()
    )

    @Test
    fun irComplex_1() = testArithmeticExpressionIR(
        """
            (2 * 30 + (60 + 70)) + (60 + 70 / ( (0-1) * 20 )) / (20 / 10)
        """.trimIndent()
    )

    @Test
    fun irComplex_2() = testArithmeticExpressionIR(
        """
            (2+ 3+ 4 +(100/(120+20+(120+0)))) - (100 / 20 / (100+30 ) - 102 +12 ) 
        """.trimIndent()
    )

    @Test
    fun irAssign() {
        testIR(
            """
            int main()
            {
                int a,b;
                a=b;
                b=a;
            }
        """.trimIndent()
        )
    }

    @Test
    fun irAssign_0() {
        testIR(
            """
            int main()
            {
                int a= 10+1;
            }
        """.trimIndent()
        )
    }


    @Test
    fun irAssign_1() {
        testIR(
            """
            int main()
            {
                int a= 10 + 20;
            }
        """.trimIndent()
        )
    }

    @Test
    fun irAssign_2() {
        testIR(
            """
            int main()
            {
                int a= 10 + 20,b=10+(20/10);
            }
        """.trimIndent()
        )
    }

    @Test
    fun irAssign_3() {
        testIR(
            """
            int main()
            {
                int a= 30 + 40,b=10+(20/10),c=1,d=200,e=(20*40);
            }
        """.trimIndent()
        )
    }

    @Test
    fun irlogical_1() = testLogicalExpressionIR("""
         a != 10
    """.trimIndent())

    @Test
    fun irlogical_2() = testLogicalExpressionIR("""
         (a != 10) && (a == 0)
    """.trimIndent())

    @Test
    fun irlogical_3() = testLogicalExpressionIR("""
         (a >= 10) || (b < 10  && ( c <= 10))
    """.trimIndent())

    @Test
    fun irlogical_4() = testLogicalExpressionIR("""
         (a == 0)
    """.trimIndent())

    @Test
    fun irlogical_5() = testLogicalExpressionIR("""
         (( a + c * 3 > b  ) && (b != 0))
    """.trimIndent())

    @Test
    fun irIf() = testIR("""
        int main()
            {
                int a=0;
                if((( a + c * 3 > b  ) && (b != 0))){
                    a = 10;
                    a = 20;
                    a = 30;
                }
                else{
                    a = 1;
                    a = 2;
                    a = 3;
                }
                a = 9999;
            }
    """.trimIndent())

    @Test
    fun irIfWithOutElse() = testIR("""
        int main()
            {
                int a=0;
                if((( a + c * 3 > b  ) && (b != 0))){
                    a = 10;
                    a = 20;
                    a = 30;
                }
                a = 9999;
            }
    """.trimIndent())

    @Test
    fun irMultiIf() = testIR("""
        int main()
            {
                int a=0;
                if((( a + c * 3 > b  ) && (b != 0))){
                    a = 10;
                    a = 20;
                    a = 30;
                }
                else if ( a > 10 ){
                    a = 1;
                }
                else {
                    a = 2;
                }
                a = 9999;
            }
    """.trimIndent())

    @Test
    fun irMultiIf_1() = testIR("""
        int main()
            {
                int a=0,b=0;
                if((( a + c * 3 > b  ) && (b != 0))){
                    a = 10;
                    a = 20;
                    a = 30;
                }
                else if ( ((a > 10 ) || ( b < 10)) ){
                    a = 1;
                }
                else if ( a < 10 ){
                    a = 2 ;
                    a = 3;
                }
                a = 9999;
            }
    """.trimIndent())

    @Test
    fun irMultiIf_2() = testIR("""
        int main()
            {
                int a=0,b=0;
                if((( a + c * 3 > b  ) && (b != 0))){
                    a = 10;
                    a = 20;
                    a = 30;
                }
                else if ( ((a > 10 ) || ( b < 10)) ){
                    a = 1;
                }
                else if ( a < 10 ){
                    a = 2 ;
                    a = 3;
                }
                else {
                    a=4;
                    a=5;
                }
                a = 9999;
            }
    """.trimIndent())

    @Test
    fun irfor() = testIR("""
        int main()
            {
                int a=0,b=0;
                for ( a = 0; a < 10; a = a+1){
                    b = b + 1;
                }
            }
    """.trimIndent())

    @Test
    fun irforIf() = testIR("""
        int main()
            {
                int a=0,b=0;
                for ( a = 0; a < 10; a = a+1){
                    if ( a > 5) {
                        b = 1;
                    }
                }
            }
    """.trimIndent())

    @Test
    fun irforIf_1() = testIR("""
        int main()
            {
                int a=0,b=0;
                for ( a = 0; a < 10; a = a+1){
                    if ( a > 5) {
                        b = 1;
                    }
                    else{
                       b = 0 ;
                    }
                }
            }
    """.trimIndent())

    @Test
    fun irforFor() = testIR("""
        int main()
            {
                int a=0,b=0,c=0;
                for ( a = 0; a < 10; a = a+1){
                    for ( b = 0 ; b < 20 ; b = b+2 ){
                        c = c + 1;
                    }
                    c = c - 1;
                }
            }
    """.trimIndent())

    @Test
    fun irforForFor () = testIR("""
        int main()
            {
                int a=0,b=0,c=0,d=0;
                for ( a = 0; a < 10; a = a+1) {
                    for ( b = 0 ; b < 20 ; b = b+2 ){
                        for( c = 0 ; c < 100 ; c = c + 1 ) {
                            c = 20;
                        }
                        c = 10;
                    }
                    c = 100;
                }
            }
    """.trimIndent())

    @Test
    fun irwhile () = testIR("""
        int main()
            {
                int a=0 , b= 0;
                while ( ((a < 10) && (b < 5)) ){
                    a = a + 1;   
                }
            }
    """.trimIndent())

    @Test
    fun irwhileWhile () = testIR("""
        int main()
            {
                int a=0,b=0;
                while ( a < 10 ){
                    a = a + 1; 
                    while ( b < 100 ){
                        b = b + 1 ;
                    }
                }
            }
    """.trimIndent())

    @Test
    fun ir () = testIR("""
        int main()
            {
                int a=0,b=0;
                for ( a = 0 ; a < 100 ; a= a+ 1 ){
                    if ( a < 50 ){
                        while ( b < 20 ){
                            b = b + 1;
                        }
                        if ( ((b >= 20) && ( a < 40 )) ){
                            b = 0;
                        }
                    }
                    else if ( a < 75 ){
                       while ( b != 20 ){
                            b = b+1;
                       }
                    }
                    else if ( a < 80 ){
                        for ( b= 0; b < 100 ; b = b+1 ){
                            b = b + 1;
                        }
                    }
                }
                a = 0;
                b = 0;
            }
    """.trimIndent())

    //测试算数表达式生成的四元式
    private fun testLogicalExpressionIR(intput: String) {
        parse(
            """
            int main()
            {
                if(${intput})
                {
                    
                }
                return 0;
            }
        """.trimIndent(), false, true
        )
    }
    //测试算数表达式生成的四元式
    private fun testArithmeticExpressionIR(intput: String) {
        parse(
            """
            int main()
            {
                int a;
                a = ${intput};
                return 0;
            }
        """.trimIndent(), false, true
        )
    }




}

