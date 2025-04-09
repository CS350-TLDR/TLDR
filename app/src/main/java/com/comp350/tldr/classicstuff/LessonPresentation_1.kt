package com.comp350.tldr.lessonScreens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp


//var textLineIndex = 0
// A custom function that handles text for lesson 1 for easy sequencing!
//@Composable
//fun nextLineOfText(
//    beforeText: String,
//    specialText: String,
//    metaText: String,
//    afterText: String,
//    textLine: String,
//    colorChoice: Color,
//    xOffset: Int,
//    yOffset: Int,
//    ): Boolean {
//    var isVisible by remember { mutableStateOf(false) }
//    var haveClicked = false
//    // Simulate some event that triggers the text to become visible
//    // (e.g., button click, data loading, etc.)
//    LaunchedEffect(Unit) {
//        kotlinx.coroutines.delay(1000) // Wait for 1 second
//        isVisible = true
//    }
//
//    // Create a clickable text
//    val clickableText = buildAnnotatedString {
//
//        append(beforeText) // Normal text that is connected before the special text.
//        // 'pushStringAnnotation' will load the string stored in 'annotation' to the stack. This will act as metadata to the next string.
//        pushStringAnnotation(tag = textLine, annotation = metaText) // 'tag' acts as an ID to be referenced for '.getStringAnnotations'
//        withStyle(style = SpanStyle(color = colorChoice, textDecoration = TextDecoration.Underline))
//        {
//            append(specialText) // Special text that is underlined and clickable
//        }
//        pop() // End speciality of text, removing it from the stack.
//        append(afterText) // Normal text that is connected after the special text
//    }
//
//    // Construct a space for the clickable text to be placed in with fade in animation
//    Column (
//        modifier = Modifier.offset(x = xOffset.dp, y = yOffset.dp), // Take up the full screen
//        verticalArrangement = Arrangement.Center, // Center vertically
//        horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
//    ) {
//        AnimatedVisibility(
//            visible = isVisible,
//            enter = fadeIn()
//        ) {
//            ClickableText(
//                text = clickableText, // The text to be displayed
//                modifier = Modifier.fillMaxWidth(),
//                style = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center),
//                onClick = { offset ->
//                    clickableText.getStringAnnotations(
//                        tag = textLine,
//                        start = offset,
//                        end = offset
//                    ) // document later
//                        .firstOrNull()?.let { annotationCalled ->
//                            println("Clicked Text: ${annotationCalled.item}")
//
////                            if (!haveClicked) {
////
////                                textLineIndex = 1 + textLine.toInt()
////
////                                when (textLineIndex) {
////
////                                   2 -> nextLineOfText(
////                                       beforeText = "Welcome to Lesson 1!\n",
////                                       specialText = "Click here",
////                                       metaText = "here",
////                                       afterText = " to progress the lesson.",
////                                       textLine = "2",
////                                       colorChoice = Color(0xFF4CAF50),
////                                       xOffset = 0,
////                                       yOffset = 30
////                                   )
////                                }
////                            }
////                            haveClicked = true
//
//                        }
//
//                }
//            )
//        }
//    }
//    return true
//}


//@Composable
//fun lineOfText_1(
//    beforeText: String,
//    specialText: String,
//    metaText: String,
//    afterText: String,
//    textLine: String,
//    colorChoice: Color,
//    xOffset: Int,
//    yOffset: Int,
//): Boolean {
//    var isVisible by remember { mutableStateOf(false) }
//    var haveClicked = false
//    // Simulate some event that triggers the text to become visible
//    // (e.g., button click, data loading, etc.)
//    LaunchedEffect(Unit) {
//        kotlinx.coroutines.delay(1000) // Wait for 1 second
//        isVisible = true
//    }
//
//    // Create a clickable text
//    val clickableText = buildAnnotatedString {
//
//        append(beforeText) // Normal text that is connected before the special text.
//        // 'pushStringAnnotation' will load the string stored in 'annotation' to the stack. This will act as metadata to the next string.
//        pushStringAnnotation(tag = textLine, annotation = metaText) // 'tag' acts as an ID to be referenced for '.getStringAnnotations'
//        withStyle(style = SpanStyle(color = colorChoice, textDecoration = TextDecoration.Underline))
//        {
//            append(specialText) // Special text that is underlined and clickable
//        }
//        pop() // End speciality of text, removing it from the stack.
//        append(afterText) // Normal text that is connected after the special text
//    }
//
//    // Construct a space for the clickable text to be placed in with fade in animation
//    Column (
//        modifier = Modifier.offset(x = xOffset.dp, y = yOffset.dp), // Take up the full screen
//        verticalArrangement = Arrangement.Center, // Center vertically
//        horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
//    ) {
//        AnimatedVisibility(
//            visible = isVisible,
//            enter = fadeIn()
//        ) {
//            ClickableText(
//                text = clickableText, // The text to be displayed
//                modifier = Modifier.fillMaxWidth(),
//                style = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center),
//                onClick = { offset ->
//                    clickableText.getStringAnnotations(
//                        tag = textLine,
//                        start = offset,
//                        end = offset
//                    ) // document later
//                        .firstOrNull()?.let { annotationCalled ->
//                            println("Clicked Text: ${annotationCalled.item}")
//
//
//                        }
//
//                }
//            )
//        }
//    }
//    return true
//}
@Composable
fun AnimatedTextVisibility() {

}
// Lesson 1 screen

@Composable
fun LessonPresentation_1(navController: NavController) {

    var isVisible by remember { mutableStateOf(true) }
    var isVisible2 by remember { mutableStateOf(false) }
    var isVisible3 by remember { mutableStateOf(false) }
    var isVisible4 by remember { mutableStateOf(false) }
    var isVisible5 by remember { mutableStateOf(false) }
    var isVisible6 by remember { mutableStateOf(false) }
    var isVisible7 by remember { mutableStateOf(false) }
    var isVisible8 by remember { mutableStateOf(false) }

//    Column {
//        Button(onClick = { isVisible = !isVisible }) {
//            Text(text = "Toggle Visibility")
//        }
//
//        AnimatedVisibility(
//            visible = isVisible,
//            enter = slideInVertically() + fadeIn(),
//            exit = slideOutVertically() + fadeOut()
//        ) {
//            Text(text = "This text animates!", fontSize = 20.sp, color = Color.Blue)
//        }
//    }

//    lineOfText_1(
//            beforeText = "Welcome to Lesson 1!\n",
//            specialText = "Click here",
//            metaText = "here",
//            afterText = " to progress the lesson.",
//            textLine = "1",
//            colorChoice = Color(0xFF4CAF50),
//            xOffset = 0,
//            yOffset = 0
//    )


//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Top, // Align to top
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Button(onClick = { isVisible = !isVisible }) {
//            Text("Toggle Text")
//        }
//
//        AnimatedVisibility(
//            visible = isVisible,
//            enter = slideInVertically(
//                initialOffsetY = { fullHeight -> fullHeight }, // Start below the screen
//                animationSpec = tween(durationMillis = 1000)
//            ) + fadeIn(
//                animationSpec = tween(durationMillis = 1000)
//            ),
//            exit = slideOutVertically(
//                targetOffsetY = { fullHeight -> fullHeight },
//                animationSpec = tween(durationMillis = 1000)
//            )
//        ) {
//            Text(
//                text = "I'm at the top!",
//                fontSize = 24.sp,
//                modifier = Modifier
//                    .offset { IntOffset(0, 0) }
//            )
//        }
//    }



    //var isVisible by remember { mutableStateOf(false) }

    // Simulate some event that triggers the text to become visible
    // (e.g., button click, data loading, etc.)
    // For this example, we'll just set isVisible to true after a delay
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000) // Wait for 1 second
        isVisible = true
    }

    val lessonStyle = TextStyle(
        fontSize = 25.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    // Create a clickable text
    val clickableText = buildAnnotatedString {
        val colorChoice = Color(0xFF4CAF50) // Choose a Color for the clickable text with hex code.
        append("Welcome to Lesson 1!\n Click ") // Normal text that is connected before the special text.
        // 'pushStringAnnotation' will load the string stored in 'annotation' to the stack. This will act as metadata to the next string.
        pushStringAnnotation(tag = "m", annotation = "here") // 'tag' acts as an ID to be referenced for '.getStringAnnotations'
        withStyle(style = SpanStyle(color = colorChoice, textDecoration = TextDecoration.Underline))
        {
            append("here") // Special text that is underlined and clickable
        }
        pop() // End speciality of text, removing it from the stack.
        append(" to progress the lesson.") // Normal text that is connected after the special text
    }

    // Construct a space for the clickable text to be placed in with fade in animation
    Column (
        modifier = Modifier.fillMaxSize(), // Take up the full screen
        verticalArrangement = Arrangement.Center, // Center vertically
        horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight }, // Start below the screen
                animationSpec = tween(durationMillis = 1000)
            ) + fadeIn(
                animationSpec = tween(durationMillis = 1500)
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> -fullHeight },
                animationSpec = tween(durationMillis = 800)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 1300)

            )

        ) {
            ClickableText(
                text = clickableText, // The text to be displayed
                modifier = Modifier.fillMaxWidth(),
                style = lessonStyle,
                onClick = { offset ->
                    clickableText.getStringAnnotations(
                        tag = "m",
                        start = offset,
                        end = offset
                    ) // document later
                        .firstOrNull()?.let { annotationCalled ->
                            println("Clicked: ${annotationCalled.item}")

                            isVisible = !isVisible
                            isVisible2 = true

                            //navController.navigate("quiz_page")
                        }

                }
            )
        }
    }

//Next Line

val clickableTextReally = buildAnnotatedString {
    val colorChoice = Color(0xFF4CAF50) // Choose a Color for the clickable text with hex code.
    append("") // Normal text that is connected before the special text.
    // 'pushStringAnnotation' will load the string stored in 'annotation' to the stack. This will act as metadata to the next string.
    pushStringAnnotation(tag = "m2", annotation = "REALLY?") // 'tag' acts as an ID to be referenced for '.getStringAnnotations'
    withStyle(style = SpanStyle(color = colorChoice, textDecoration = TextDecoration.Underline, fontStyle = FontStyle.Italic, fontSize = 40.sp))
    {
        append("REALLY?") // Special text that is underlined and clickable
    }
    pop() // End speciality of text, removing it from the stack.
    append("\n\n\nWhat are variables used for? ") // Normal text that is connected after the special text
}

// Construct a space for the clickable text to be placed in with fade in animation
Column (
modifier = Modifier.fillMaxSize(), // Take up the full screen
verticalArrangement = Arrangement.Center, // Center vertically
horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
) {
    AnimatedVisibility(
        visible = isVisible2,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight - 200 }, // Start below the screen
            animationSpec = tween(durationMillis = 2000)
        ) + fadeIn(
            animationSpec = tween(durationMillis = 2000)
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(durationMillis = 800)
        ) + fadeOut(
            animationSpec = tween(durationMillis = 1000)
        )

    ) {
        ClickableText(
            text = clickableTextReally, // The text to be displayed
            modifier =  Modifier.graphicsLayer { rotationZ = 7f } .offset { IntOffset(-8, -150) },
            style = lessonStyle,
            onClick = { offset ->
                clickableTextReally.getStringAnnotations(
                    tag = "m2",
                    start = offset,
                    end = offset
                ) // document later
                    .firstOrNull()?.let { annotationCalled ->
                        println("Clicked: ${annotationCalled.item}")
                        isVisible2 = !isVisible2
                        isVisible3 = true
                        //navController.navigate("quiz_page")
                    }

            }
        )
    }
}
    //Line 3
    val clickableTextHoldData = buildAnnotatedString {
        val colorChoice = Color(0xFF4CAF50) // Choose a Color for the clickable text with hex code.
        append("They\n store\n ") // Normal text that is connected before the special text.
        // 'pushStringAnnotation' will load the string stored in 'annotation' to the stack. This will act as metadata to the next string.
        pushStringAnnotation(tag = "m2", annotation = "DATA!") // 'tag' acts as an ID to be referenced for '.getStringAnnotations'
        withStyle(style = SpanStyle(color = colorChoice, textDecoration = TextDecoration.Underline, fontStyle = FontStyle.Italic, fontSize = 40.sp))
        {
            append("DATA!!!") // Special text that is underlined and clickable
        }
        pop() // End speciality of text, removing it from the stack.
        append("") // Normal text that is connected after the special text
    }

// Construct a space for the clickable text to be placed in with fade in animation
    Column (
        modifier = Modifier.fillMaxSize(), // Take up the full screen
        verticalArrangement = Arrangement.Center, // Center vertically
        horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
    ) {
        AnimatedVisibility(
            visible = isVisible3,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight + 200 }, // Start below the screen
                animationSpec = tween(durationMillis = 50)
            ) + fadeIn(
                animationSpec = tween(durationMillis = 100)
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> -fullHeight },
                animationSpec = tween(durationMillis = 800)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 2000)
            )

        ) {
            ClickableText(
                text = clickableTextHoldData, // The text to be displayed
                modifier =  Modifier.graphicsLayer { rotationX = 30f } .offset { IntOffset(-8, -150) },
                style = lessonStyle,
                onClick = { offset ->
                    clickableTextHoldData.getStringAnnotations(
                        tag = "m2",
                        start = offset,
                        end = offset
                    ) // document later
                        .firstOrNull()?.let { annotationCalled ->
                            println("Clicked: ${annotationCalled.item}")
                            isVisible3 = !isVisible3

                            isVisible4 = true
                            //navController.navigate("quiz_page")
                        }

                }
            )
        }
    }
    //Line 4
    val clickableTextVariables = buildAnnotatedString {
        val colorChoice = Color(0xFF4CAF50) // Choose a Color for the clickable text with hex code.
        append("Your variables should look like a snake") // Normal text that is connected before the special text.
        // 'pushStringAnnotation' will load the string stored in 'annotation' to the stack. This will act as metadata to the next string.
        pushStringAnnotation(tag = "m2", annotation = "DATA!") // 'tag' acts as an ID to be referenced for '.getStringAnnotations'
        withStyle(style = SpanStyle(color = colorChoice, fontStyle = FontStyle.Italic, fontSize = 25.sp))
        {
            append("_case-_-_-_-") // Special text that is underlined and clickable
        }
        pop() // End speciality of text, removing it from the stack.
        append("") // Normal text that is connected after the special text
    }

// Construct a space for the clickable text to be placed in with fade in animation
    Column (
        modifier = Modifier.fillMaxSize(), // Take up the full screen
        verticalArrangement = Arrangement.Center, // Center vertically
        horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
    ) {
        AnimatedVisibility(
            visible = isVisible4,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight + 200 }, // Start below the screen
                animationSpec = tween(durationMillis = 50)
            ) + fadeIn(
                animationSpec = tween(durationMillis = 100)
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> -fullHeight },
                animationSpec = tween(durationMillis = 800)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 2000)
            )

        ) {
            ClickableText(
                text = clickableTextVariables, // The text to be displayed
                modifier =  Modifier.graphicsLayer { rotationY = -14f } .offset { IntOffset(-60, -150) },
                style = lessonStyle,
                onClick = { offset ->
                    clickableTextVariables.getStringAnnotations(
                        tag = "m2",
                        start = offset,
                        end = offset
                    ) // document later
                        .firstOrNull()?.let { annotationCalled ->
                            println("Clicked: ${annotationCalled.item}")
                            isVisible4 = !isVisible4

                            isVisible5 = true
                            //navController.navigate("quiz_page")
                        }

                }
            )
        }
    }
    //Line 5
    val clickableTextAssign = buildAnnotatedString {
        val colorChoice = Color(0xFF4CAF50) // Choose a Color for the clickable text with hex code.
        append("Speaking of snakes!\n In Python you can assign variables simply as, ") // Normal text that is connected before the special text.
        // 'pushStringAnnotation' will load the string stored in 'annotation' to the stack. This will act as metadata to the next string.
        pushStringAnnotation(tag = "m2", annotation = "DATA!") // 'tag' acts as an ID to be referenced for '.getStringAnnotations'
        withStyle(style = SpanStyle(color = colorChoice, fontStyle = FontStyle.Italic, fontSize = 25.sp))
        {
            append("Your_Variable = Data\n") // Special text that is underlined and clickable
        }
        pop() // End speciality of text, removing it from the stack.
        append("\n Data being anything like numbers or words... pardon, integers and strings respectively.") // Normal text that is connected after the special text
    }

// Construct a space for the clickable text to be placed in with fade in animation
    Column (
        modifier = Modifier.fillMaxSize(), // Take up the full screen
        verticalArrangement = Arrangement.Center, // Center vertically
        horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
    ) {
        AnimatedVisibility(
            visible = isVisible5,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight + 200 }, // Start below the screen
                animationSpec = tween(durationMillis = 50)
            ) + fadeIn(
                animationSpec = tween(durationMillis = 100)
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> -fullHeight },
                animationSpec = tween(durationMillis = 800)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 2000)
            )

        ) {
            ClickableText(
                text = clickableTextAssign, // The text to be displayed
                modifier =  Modifier.graphicsLayer { rotationX = 5f } .offset { IntOffset(0, -150) },
                style = lessonStyle,
                onClick = { offset ->
                    clickableTextAssign.getStringAnnotations(
                        tag = "m2",
                        start = offset,
                        end = offset
                    ) // document later
                        .firstOrNull()?.let { annotationCalled ->
                            println("Clicked: ${annotationCalled.item}")
                            isVisible5 = !isVisible5

                            isVisible6 = true
                            //navController.navigate("quiz_page")
                        }

                }
            )
        }
    }
    //Line 6
    val clickableTextQuote = buildAnnotatedString {
        val colorChoice = Color(0xFF4CAF50) // Choose a Color for the clickable text with hex code.
        append("Now for rapid fire!\n \"Quotations\" show that it is a string!\n Defining a function in Python is def, short for define.\n " +
                "printing text is literally print(DATA).\n And commenting is done with #.\n  ") // Normal text that is connected before the special text.
        // 'pushStringAnnotation' will load the string stored in 'annotation' to the stack. This will act as metadata to the next string.
        pushStringAnnotation(tag = "m2", annotation = "DATA!") // 'tag' acts as an ID to be referenced for '.getStringAnnotations'
        withStyle(style = SpanStyle(color = colorChoice, fontStyle = FontStyle.Italic, fontSize = 25.sp))
        {
            append("You are ready!!! Good luck on the Quiz!!!") // Special text that is underlined and clickable
        }
        pop() // End speciality of text, removing it from the stack.
        append("") // Normal text that is connected after the special text
    }

// Construct a space for the clickable text to be placed in with fade in animation
    Column (
        modifier = Modifier.fillMaxSize(), // Take up the full screen
        verticalArrangement = Arrangement.Center, // Center vertically
        horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
    ) {
        AnimatedVisibility(
            visible = isVisible6,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight + 200 }, // Start below the screen
                animationSpec = tween(durationMillis = 50)
            ) + fadeIn(
                animationSpec = tween(durationMillis = 100)
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> -fullHeight },
                animationSpec = tween(durationMillis = 800)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 2000)
            )

        ) {
            ClickableText(
                text = clickableTextQuote, // The text to be displayed
                modifier =  Modifier.graphicsLayer { rotationX = 5f } .offset { IntOffset(0, -150) },
                style = lessonStyle,
                onClick = { offset ->
                    clickableTextQuote.getStringAnnotations(
                        tag = "m2",
                        start = offset,
                        end = offset
                    ) // document later
                        .firstOrNull()?.let { annotationCalled ->
                            println("Clicked: ${annotationCalled.item}")
                            isVisible6 = !isVisible6


                            navController.navigate("quiz_page")
                        }

                }
            )
        }
    }
}




//---------Preview Screen---------
@Preview
@Composable
fun LessonPresentation_1Preview() {
    LessonPresentation_1(navController = NavController(LocalContext.current))}