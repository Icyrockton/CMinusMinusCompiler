package compiler

import compiler.Token.Companion.isLegalIdentifier
import compiler.Token.Companion.prettyPrint
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @Author : Icyrockton
 * @Date: 2021/6/2 20:42
 */
class LexAnalyzerTest {
    private val lexAnalyzer = LexAnalyzer()

    @Test
    fun testWhile() {
        parse(
            """
            int main(){
                int a=0,w_wawda=101;
                int b=0,w111=0;
                int c=100;
                int d=10,e=102;
                return 0;
            }
        """.trimIndent()
        ).prettyPrint()
    }

    fun parse(input: String): List<Token> = lexAnalyzer.parse(input)

    @Test
    fun testUnresolvedID() {
        parse(
            """
            int main(){
               int w_22,dwa;
            }
        """.trimIndent()
        ).prettyPrint()
    }

    @Test
    fun testString() {
        parse(
            """
            int main(){
                string a="Gdwac!@#$%^&*()_+",s="wawa",g="wadwaswa!!!wsaswa";
                int a=10;
                int b=20;
                int c="12121";
                return 0;
            }
        """.trimIndent()
        ).prettyPrint()
    }


    @Test
    fun testOperator() {
        parse(
            """
            int main(){
                int a=20+30,b=100+50;
                int c=1++,d=--2;
                int g="wadwadaw" + "wdadwawad" ;
        """.trimIndent()
        ).prettyPrint()
    }

    @Test
    fun testOperator_1() {
        parse(
            """
            int main(){
                int d=--2;
        """.trimIndent()
        ).prettyPrint()
    }

    @Test
    fun testOperator_2() {
        parse(
            """
            int main(){
                bool a >= 10 , b <= 10 ;
                bool a > 10 ;
                
        """.trimIndent()
        ).prettyPrint()
    }

    @Test
    fun testOperator_3() {
        parse(
            """
            int main(){
                bool a = b > 10 && c < 100;
                bool d= a >= 20 || g >= 100;
                return 0;    
            }
                
        """.trimIndent()
        ).prettyPrint()
    }


    @Test
    fun testUnresolvedString() {
        parse(
            """
            int main(){
                string a="wdadwadwa;
                return 0;
            }
        """.trimIndent()
        ).prettyPrint()
    }

    @Test
    fun testComment() {
        parse(
            """
            int main(){
                int a = 20 ; //这是a的注释
                /* 123
                测试多行注释
                1234 ***
                还好吗
                ***/
                return 0 ;
            }
            // hahahwadwa
            // end
        """.trimIndent()
        ).prettyPrint()
    }

    @Test
    fun testIf() {
        parse(
            """
            int main(){
                int c = 10;
                if(c >= 20){
                    int b = 1;   //注释
                }
                else{
                    int g = 20 //dwadwadwa
                }
                return 0 ;
            }
        """.trimIndent()
        ).prettyPrint()
    }

    @Test
    fun testReturn() {
        parse(
            """
            int main(){
                return 0;
            }
        """.trimIndent()
        ).prettyPrint()
    }

    @Test
    fun testMultiAssign(){
        parse("""
            int main(){
                int a=10 , b= 20 , c= 100 ;
                return 0;
             }
        """.trimIndent()).prettyPrint()
    }

    @Test
    fun testMss(){
        parse("""
            int main(){
                if( !a){
                }
                return 0;
             }
        """.trimIndent()).prettyPrint()
    }


    @Test
    fun testIfLogical() {
        parse(
            """
            int main(){
                int a = 10 ;
                int b = 20;
                if ( (( a > 10 ) && ( b < 10 )) ) {
                    a = 5;
                 
                }
                else 
                    a=5;
                return 0;
             }
        """.trimIndent()
        ).prettyPrint()
    }





    @Test
    fun testID() {
        assertEquals(true, "a".isLegalIdentifier())
        assertEquals(true, "a1".isLegalIdentifier())
        assertEquals(true, "aaaa_111".isLegalIdentifier())
        assertEquals(true, "aa1_dwwad_1".isLegalIdentifier())
        assertEquals(false, "111".isLegalIdentifier())
        assertEquals(false, "1_2e2e2".isLegalIdentifier())
        assertEquals(true, "w_1".isLegalIdentifier())
        assertEquals(true, "_111".isLegalIdentifier())
    }
}