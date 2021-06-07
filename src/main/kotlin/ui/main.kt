import androidx.compose.desktop.Window
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compiler.*
import kotlinx.coroutines.launch

fun main() = Window(title = "C--编译器 Icyrockton", resizable = false, size = IntSize(1400, 800)) {

    var code by remember { mutableStateOf(sample_1) }
    val lexAnalyzer = remember { LexAnalyzer() }
    val llParser = remember { LLParser() }
    val tokens = remember { mutableStateListOf<Token>() }
    val quadruples = remember { mutableStateListOf<Quadruples>() }
    val coroutineScope = rememberCoroutineScope()
    var parserInfo by remember { mutableStateOf("未进行语法分析") }
    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize().background(Color(0XffF2F2F2))) {
            Column(modifier = Modifier.weight(0.33f).fillMaxHeight()) {
                Text(text = "C--编辑器", color = Color.Black, modifier = Modifier.padding(start = 10.dp, top = 5.dp))
                TextField(
                    textStyle = TextStyle(color = Color.Black, fontSize = 20.sp),
                    value = code,
                    onValueChange = { code = it },
                    colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
                    modifier = Modifier.fillMaxWidth().weight(1.0f).padding(10.dp),
                    placeholder = { Text("输入C--代码") }
                )
                Column(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().weight(0.25f).padding(5.dp)) {
                        Button({
                            code = sample_1
                        }, modifier = Modifier.weight(0.33f).padding(horizontal = 3.dp)) {
                            Text("示例1")
                        }
                        Button({
                            code = sample_2
                        }, modifier = Modifier.weight(0.33f).padding(horizontal = 3.dp)) {
                            Text("示例2")
                        }
                        Button({
                            code = sample_3
                        }, modifier = Modifier.weight(0.33f).padding(horizontal = 3.dp)) {
                            Text("示例3")
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth().weight(0.25f).padding(5.dp)) {
                        Button({
                            code = sample_4
                        }, modifier = Modifier.weight(0.33f).padding(horizontal = 3.dp)) {
                            Text("示例4")
                        }
                        Button({
                            code = sample_5
                        }, modifier = Modifier.weight(0.33f).padding(horizontal = 3.dp)) {
                            Text("示例5")
                        }
                        Button({
                            code = sample_6
                        }, modifier = Modifier.weight(0.33f).padding(horizontal = 3.dp)) {
                            Text("示例6")
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth().weight(0.25f).padding(5.dp)) {
                        Button({
                            coroutineScope.launch {
                                //词法分析
                                tokens.clear()
                                tokens.addAll(lexAnalyzer.parse(code))
                            }
                        }, modifier = Modifier.weight(0.33f).padding(horizontal = 3.dp)) {
                            Text("词法分析")
                        }
                        Button({
                            coroutineScope.launch {
                                try {
                                    llParser.parse(lexAnalyzer.parse(code))
                                    parserInfo = "语法分析通过"
                                }
                                catch (e : LLParserException){
                                    parserInfo = e.toString()
                                }

                            }
                        }, modifier = Modifier.weight(0.33f).padding(horizontal = 3.dp)) {
                            Text("语法分析")
                        }
                        Button({
                               coroutineScope.launch {
                                   if (parserInfo == "语法分析通过"){
                                       quadruples.clear()
                                       quadruples.addAll(llParser.quadruplesList)
                                   }
                               }
                        }, modifier = Modifier.weight(0.33f).padding(horizontal = 3.dp)) {
                            Text("中间代码生成")
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth().weight(0.25f).padding(5.dp)) {
                        Button({
                            coroutineScope.launch {
                                try {
                                    //词法分析
                                    tokens.clear()
                                    tokens.addAll(lexAnalyzer.parse(code))
                                    llParser.parse(tokens)
                                    parserInfo = "语法分析通过"
                                    quadruples.clear()
                                    quadruples.addAll(llParser.quadruplesList)
                                }
                                catch (e : LLParserException){
                                    parserInfo = e.toString()
                                }
                            }
                        }, modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp)) {
                            Text("一键编译")
                        }
                    }
                    }
            }
            Spacer(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.Black))
            Column(modifier = Modifier.weight(0.33f).fillMaxHeight()) {
                Column(modifier = Modifier.fillMaxWidth().weight(0.7f).padding(bottom = 10.dp)) {
                    Text(text = "词法分析", color = Color.Black, modifier = Modifier.padding(start = 10.dp, top = 5.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().height(40.dp).padding(top = 5.dp).padding(horizontal = 10.dp)
                            .background(Color.White), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "类型", modifier = Modifier.weight(0.33f), textAlign = TextAlign.Center)
                        Text(text = "行号", modifier = Modifier.weight(0.33f), textAlign = TextAlign.Center)
                        Text(text = "Token值", modifier = Modifier.weight(0.33f), textAlign = TextAlign.Center)
                    }
                    Spacer(
                        modifier = Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 10.dp)
                            .background(Color.Black)
                    )
                    val state = rememberLazyListState()
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1.0f).padding(horizontal = 10.dp)
                            .background(Color.White)
                    ) {
                        LazyColumn(Modifier.fillMaxSize().background(Color.White), state) {
                            items(tokens) { token ->
                                TokenItem(token)
                            }
                        }
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(
                                scrollState = state
                            )
                        )
                    }

                }
                Column(modifier = Modifier.fillMaxWidth().weight(0.3f)) {
                    Text(text = "语法分析", color = Color.Black, modifier = Modifier.padding(start = 10.dp, top = 5.dp))

                    TextField(value = parserInfo,readOnly = true,onValueChange = {},
                        textStyle = TextStyle(color = Color.Black, fontSize = 20.sp),
                        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
                        modifier = Modifier.fillMaxWidth().weight(1.0f).padding(10.dp),
                        )
                }

            }
            Spacer(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.Black))
            Column(modifier = Modifier.weight(0.33f).fillMaxHeight()) {
                Column(modifier = Modifier.fillMaxWidth().weight(1.0f)) {
                    Text(
                        text = "中间代码(IR)生成 / 四元式",
                        color = Color.Black,
                        modifier = Modifier.padding(start = 10.dp, top = 5.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().height(40.dp).padding(top = 5.dp).padding(horizontal = 10.dp)
                            .background(Color.White), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "编号", modifier = Modifier.weight(0.2f), textAlign = TextAlign.Center)
                        Text(text = "OP", modifier = Modifier.weight(0.2f), textAlign = TextAlign.Center)
                        Text(text = "ARG1", modifier = Modifier.weight(0.2f), textAlign = TextAlign.Center)
                        Text(text = "ARG2", modifier = Modifier.weight(0.2f), textAlign = TextAlign.Center)
                        Text(text = "RESULT", modifier = Modifier.weight(0.2f), textAlign = TextAlign.Center)
                    }
                    Spacer(
                        modifier = Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 10.dp)
                            .background(Color.Black)
                    )
                    val state = rememberLazyListState()
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1.0f).padding(horizontal = 10.dp).padding(bottom = 10.dp)
                            .background(Color.Black)
                    ) {
                        LazyColumn(Modifier.fillMaxSize().background(Color.White), state) {
                            items(quadruples) { quadruple ->
                                QuadrupleItem(quadruple)
                            }
                        }
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(
                                scrollState = state
                            )
                        )
                    }
                }

            }
        }
    }
}
@Composable
fun QuadrupleItem(quadruples: Quadruples) {
    Row(
        modifier = Modifier.fillMaxWidth().height(40.dp).padding(horizontal = 10.dp).clickable { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = quadruples.id.toString(), modifier = Modifier.weight(0.2f), textAlign = TextAlign.Center)
        Text(text = quadruples.op, modifier = Modifier.weight(0.2f), textAlign = TextAlign.Center)
        Text(text = quadruples.arg1  ?: "", modifier = Modifier.weight(0.2f), textAlign = TextAlign.Center)
        Text(text = quadruples.arg2 ?: "", modifier = Modifier.weight(0.2f), textAlign = TextAlign.Center)
        Text(text = quadruples.result ?: "", modifier = Modifier.weight(0.2f), textAlign = TextAlign.Center)
    }
}
@Composable
fun TokenItem(token: Token) {
    Row(
        modifier = Modifier.fillMaxWidth().height(40.dp).padding(horizontal = 10.dp).clickable { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = token.type.toString(), modifier = Modifier.weight(0.33f), textAlign = TextAlign.Center)
        Text(text = token.line.toString(), modifier = Modifier.weight(0.33f), textAlign = TextAlign.Center)
        Text(text = token.value ?: "", modifier = Modifier.weight(0.33f), textAlign = TextAlign.Center)
    }
}

val sample_1 =
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

val sample_2 = """
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
""".trimIndent()

val sample_3 = """
    int main()
    {
          int a=0,b=0,c=0,d=0;
          for ( a = 0; a < 10; a = a+1) {
              for ( b = 0 ; b < 20 ; b = b+2 ){
                  for( c = 0 ; c < 100 ; c = c + 1 ) {
                      if ( d < 20 ){
                          c = 20;
                      }
                      while ( d < 20 ){
                           d = d + 1;
                           if ( d > 10 ){
                                d = d + 1;
                           }
                           else if ( d > 5 ){
                                d = d + 2;
                           }
                           else {
                                d = d + 3;
                           }
                      }
                  }
                  c = 10;
              }
              c = 100;
          }
          a = 0;
          if ( a == 0){
              a = 1010;
          }
    }
""".trimIndent()

val sample_4 ="""
    int main(){
                int a = 10 ;
                int d = 20 , e = 100;
                while ( (d < 100) && ((e <= 100) || (a >= 10)) ){
                    d = d + 1;
                    if ( e >= 100){
                        e = e - 10;
                        while ( a >= 10 ){
                            
                        }
                    }
                    else if ( e >= 70 ){ 
                        e = e - 1;
                    }
                    else 
                        e = e - 0;
                }
                for ( d = 20 ; (d <= 100 ) && (a <= 100) ; d = d+10 ){
                    d = d+1;
                }
                d = 0;
                a = 200;
                return 0;
             }
""".trimIndent()

val sample_5 ="""
    int main(){
                int a = 10 ;
                int d = 20 , e = 100 , f = 100;
                
                for ( d = 20 ; (d <= 100 ) && (a <= 100) ; d = d+10 ){
                    d = d+1;
                }
                while ( e >= 100 ){
                    if ( e >= 150){
                        d = d-1;
                    }
                    else if ( ((e >= 200) || (f != 100)) ){
                        d = d+1;
                    }
                }
                d = 0;
                a = 200;
                return 0;
             }
""".trimIndent()


val sample_6 ="""
    int main(){
                int a = 10 , fff=0 ;
                int d = 20 , e = 100 , f = 100;
                
                while ( a <= 100){
                    d = a + b;
                    b = a ;
                    a = 10 ;
                }
                return 0;
             }
""".trimIndent()