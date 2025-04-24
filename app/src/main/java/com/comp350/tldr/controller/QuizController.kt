package com.comp350.tldr.controllers

import android.content.Context
import android.content.Intent
import com.comp350.tldr.models.Question
import com.comp350.tldr.model.services.PopupService

class QuizController(private val context: Context) {
    private val userController = UserController(context)

    // Question repositories
    private val pythonQuestions = listOf(
        Question(
            "What are variables used for?",
            listOf("To store data", "To print messages", "To create loops", "To define classes"),
            0
        ),
        Question(
            "What is the correct form to name a variable with multiple words?",
            listOf("snake_case", "PascalCase", "camelCase", "UPPER_CASE"),
            0
        ),
        // Additional questions from the original code
        Question(
            "What keyword is used to define a function in Python?",
            listOf("function", "def", "define", "func"),
            1
        ),
        Question(
            "How do you print text in Python?",
            listOf(
                "echo(\"Hello\")",
                "console.log(\"Hello\")",
                "System.out.println(\"Hello\")",
                "print(\"Hello\")"
            ),
            3
        ),
        Question(
            "What is the correct way to start a comment in Python?",
            listOf(
                "// This is a comment",
                "<!-- This is a comment -->",
                "# This is a comment",
                "/* This is a comment */"
            ),
            2
        ),
        Question(
            "What data type would you use to store a whole number in Python?",
            listOf("float", "str", "int", "bool"),
            2
        ),
        Question(
            "How do you create a list in Python?",
            listOf(
                "my_list = (1, 2, 3)",
                "my_list = [1, 2, 3]",
                "my_list = {1, 2, 3}",
                "my_list = <1, 2, 3>"
            ),
            1
        ),
        Question(
            "Which of these is NOT a Python data type?",
            listOf("list", "dictionary", "array", "tuple"),
            2
        ),
        Question(
            "How do you check the length of a list in Python?",
            listOf(
                "list.size()",
                "size(list)",
                "len(list)",
                "list.length"
            ),
            2
        ),
        Question(
            "What will print(type(42)) display in Python?",
            listOf(
                "<class 'int'>",
                "<class 'string'>",
                "<class 'float'>",
                "<class 'number'>"
            ),
            0

        ),
        // Python Basics
        Question(
            "What are variables used for?",
            listOf("To store data", "To print messages", "To create loops", "To define classes"),
            0
        ),
        Question(
            "What is the correct form to name a variable with multiple words?",
            listOf("snake_case", "PascalCase", "camelCase", "UPPER_CASE"),
            0
        ),
        Question(
            "What keyword is used to define a function in Python?",
            listOf("function", "def", "define", "func"),
            1
        ),
        Question(
            "How do you print text in Python?",
            listOf(
                "echo(\"Hello\")",
                "console.log(\"Hello\")",
                "System.out.println(\"Hello\")",
                "print(\"Hello\")"
            ),
            3
        ),
        Question(
            "What is the correct way to start a comment in Python?",
            listOf(
                "// This is a comment",
                "<!-- This is a comment -->",
                "# This is a comment",
                "/* This is a comment */"
            ),
            2
        ),

        // Python Data Types
        Question(
            "What data type would you use to store a whole number in Python?",
            listOf("float", "str", "int", "bool"),
            2
        ),
        Question(
            "How do you create a list in Python?",
            listOf(
                "my_list = (1, 2, 3)",
                "my_list = [1, 2, 3]",
                "my_list = {1, 2, 3}",
                "my_list = <1, 2, 3>"
            ),
            1
        ),
        Question(
            "Which of these is NOT a Python data type?",
            listOf("list", "dictionary", "array", "tuple"),
            2
        ),
        Question(
            "How do you check the length of a list in Python?",
            listOf(
                "list.size()",
                "size(list)",
                "len(list)",
                "list.length"
            ),
            2
        ),
        Question(
            "What will print(type(42)) display in Python?",
            listOf(
                "<class 'int'>",
                "<class 'string'>",
                "<class 'float'>",
                "<class 'number'>"
            ),
            0
        ),

        // Python Strings
        Question(
            "How do you create a multi-line string in Python?",
            listOf(
                "Using triple quotes (''' or \"\"\")",
                "Using the newline character (\\n)",
                "Using multiple print statements",
                "Python doesn't support multi-line strings"
            ),
            0
        ),
        Question(
            "What is the result of 'Hello' + ' ' + 'World' in Python?",
            listOf(
                "Hello World",
                "Hello+World",
                "HelloWorld",
                "Error: cannot concatenate strings"
            ),
            0
        ),
        Question(
            "Which method converts a string to lowercase in Python?",
            listOf(
                "str.toLower()",
                "str.lowercase()",
                "str.lower()",
                "lowercase(str)"
            ),
            2
        ),
        Question(
            "How do you get the first character of a string in Python?",
            listOf(
                "string.first()",
                "string[0]",
                "string(0)",
                "first(string)"
            ),
            1
        ),
        Question(
            "What does the strip() method do in Python?",
            listOf(
                "Removes characters from the string",
                "Removes whitespace from the beginning and end",
                "Splits the string into a list",
                "Converts the string to lowercase"
            ),
            1
        ),

        // Python Lists
        Question(
            "How do you add an item to the end of a list in Python?",
            listOf(
                "list.add(item)",
                "list.append(item)",
                "list.insert(item)",
                "list.push(item)"
            ),
            1
        ),
        Question(
            "What is the correct way to access the third item in a list?",
            listOf(
                "list[3]",
                "list[2]",
                "list.get(3)",
                "list.item(3)"
            ),
            1
        ),
        Question(
            "How do you remove an item from a list in Python?",
            listOf(
                "list.delete(item)",
                "list.remove(item)",
                "delete list[index]",
                "remove(list, item)"
            ),
            1
        ),
        Question(
            "What method sorts a list in ascending order?",
            listOf(
                "list.sort()",
                "sort(list)",
                "list.order()",
                "list.arrange()"
            ),
            0
        ),
        Question(
            "How do you make a copy of a list in Python?",
            listOf(
                "list.copy()",
                "copy(list)",
                "list.clone()",
                "list[:]"
            ),
            0
        ),

        // Python Dictionaries
        Question(
            "How do you create an empty dictionary in Python?",
            listOf(
                "dict()",
                "{}",
                "[]",
                "new Dictionary()"
            ),
            1
        ),
        Question(
            "How do you access a value in a dictionary?",
            listOf(
                "dict.get('key')",
                "dict['key']",
                "dict('key')",
                "dict->key"
            ),
            1
        ),
        Question(
            "How do you add a new key-value pair to a dictionary?",
            listOf(
                "dict.add('key', value)",
                "dict['key'] = value",
                "dict.update('key', value)",
                "dict.set('key', value)"
            ),
            1
        ),
        Question(
            "What happens if you try to access a key that doesn't exist in a dictionary?",
            listOf(
                "Returns None",
                "Raises a KeyError",
                "Creates the key with a null value",
                "Returns an empty string"
            ),
            1
        ),
        Question(
            "Which method returns a list of all keys in a dictionary?",
            listOf(
                "dict.getKeys()",
                "dict.keySet()",
                "dict.keys()",
                "keys(dict)"
            ),
            2
        ),

        // Python Control Flow
        Question(
            "What is the correct syntax for an if statement in Python?",
            listOf(
                "if (condition) { code }",
                "if condition { code }",
                "if condition: code",
                "if (condition): code"
            ),
            2
        ),
        Question(
            "How do you write a for loop to iterate over a list in Python?",
            listOf(
                "for (i = 0; i < len(list); i++): code",
                "for i in range(list): code",
                "for item in list: code",
                "for each item in list: code"
            ),
            2
        ),
        Question(
            "What is the correct way to write an infinite loop in Python?",
            listOf(
                "while True: code",
                "while (1): code",
                "loop: code",
                "for (;;): code"
            ),
            0
        ),
        Question(
            "How do you exit a loop prematurely in Python?",
            listOf(
                "exit loop",
                "break",
                "continue",
                "return"
            ),
            1
        ),
        Question(
            "What does the 'pass' statement do in Python?",
            listOf(
                "Skips to the next iteration of a loop",
                "Exits a function",
                "Does nothing (placeholder)",
                "Passes a value to the next block"
            ),
            2
        ),

        // Python Functions
        Question(
            "What is a correct way to define a function that takes two parameters?",
            listOf(
                "function my_func(a, b): code",
                "def my_func(a, b): code",
                "def my_func(a, b) { code }",
                "function my_func(a, b) { code }"
            ),
            1
        ),
        Question(
            "How do you specify a default value for a function parameter?",
            listOf(
                "def func(param = default): code",
                "def func(param: default): code",
                "def func(param <= default): code",
                "def func(param; default): code"
            ),
            0
        ),
        Question(
            "What is the purpose of the return statement in a function?",
            listOf(
                "To exit the function",
                "To send a value back to the caller",
                "To print the result",
                "To restart the function"
            ),
            1
        ),
        Question(
            "What happens if a function doesn't have a return statement?",
            listOf(
                "It returns 0",
                "It returns None",
                "It returns an error",
                "It returns the last computed value"
            ),
            1
        ),
        Question(
            "How do you call a function in Python?",
            listOf(
                "call function_name()",
                "function_name()",
                "invoke function_name()",
                "function_name.call()"
            ),
            1
        ),

        // Python Modules & Packages
        Question(
            "How do you import a module in Python?",
            listOf(
                "import module",
                "include module",
                "require module",
                "using module"
            ),
            0
        ),
        Question(
            "How do you import only a specific function from a module?",
            listOf(
                "import function from module",
                "from module include function",
                "from module import function",
                "using function from module"
            ),
            2
        ),
        Question(
            "What is the purpose of __init__.py files in package directories?",
            listOf(
                "To initialize variables",
                "To mark directories as Python packages",
                "To set up logging",
                "To create documentation"
            ),
            1
        ),
        Question(
            "What tool is commonly used to install Python packages?",
            listOf(
                "apt-get",
                "npm",
                "pip",
                "installer"
            ),
            2
        ),
        Question(
            "What file contains the specification for Python package dependencies?",
            listOf(
                "setup.py",
                "requirements.txt",
                "package.json",
                "dependencies.cfg"
            ),
            1
        ),

        // Python OOP
        Question(
            "How do you define a class in Python?",
            listOf(
                "class MyClass { }",
                "class MyClass():",
                "def class MyClass():",
                "create class MyClass:"
            ),
            1
        ),
        Question(
            "What is the first parameter typically called in a method definition?",
            listOf(
                "this",
                "self",
                "me",
                "instance"
            ),
            1
        ),
        Question(
            "How do you create an instance of a class?",
            listOf(
                "new MyClass()",
                "MyClass.create()",
                "MyClass()",
                "instance = MyClass"
            ),
            2
        ),
        Question(
            "What does inheritance mean in object-oriented programming?",
            listOf(
                "Creating multiple instances of a class",
                "A class can be based on another class",
                "Sharing variables between classes",
                "Using private variables"
            ),
            1
        ),
        Question(
            "What is the purpose of the __init__ method in a class?",
            listOf(
                "To initialize the class when it's imported",
                "To define class variables",
                "To initialize an object when it's created",
                "To clean up when an object is destroyed"
            ),
            2
        ),

        // Python File Handling
        Question(
            "How do you open a file for reading in Python?",
            listOf(
                "file = File('filename', 'r')",
                "file = open('filename', 'r')",
                "file = read('filename')",
                "file = load('filename')"
            ),
            1
        ),
        Question(
            "What method do you use to read all lines from a file into a list?",
            listOf(
                "file.read_lines()",
                "file.readlines()",
                "file.get_lines()",
                "file.read().splitlines()"
            ),
            1
        ),
        Question(
            "How should you close a file after opening it?",
            listOf(
                "file.close()",
                "close(file)",
                "file.exit()",
                "del file"
            ),
            0
        ),
        Question(
            "What's the safest way to work with files in Python?",
            listOf(
                "Using try/except blocks",
                "Using with statement (context manager)",
                "Using global error handlers",
                "Using file.safe_mode()"
            ),
            1
        ),
        Question(
            "Which mode would you use to append to a file?",
            listOf(
                "'a'",
                "'w'",
                "'r+'",
                "'add'"
            ),
            0
        ),

        // Advanced Python
        Question(
            "What is a Python generator?",
            listOf(
                "A function that creates new functions",
                "A tool that generates Python code",
                "A function that uses 'yield' instead of 'return'",
                "A special type of class constructor"
            ),
            2
        ),
        Question(
            "What is a decorator in Python?",
            listOf(
                "A design pattern for creating objects",
                "A function that modifies another function",
                "A way to add comments to code",
                "A type of Python package"
            ),
            1
        ),
        Question(
            "What is the purpose of 'lambda' functions in Python?",
            listOf(
                "To create anonymous, inline functions",
                "To organize code into multiple lines",
                "To implement polymorphism",
                "To provide backward compatibility"
            ),
            0
        ),
        Question(
            "What does the 'async' keyword do in Python?",
            listOf(
                "Makes functions run in a separate thread",
                "Marks a function as potentially blocking",
                "Declares an asynchronous function",
                "Creates a multi-process application"
            ),
            2
        ),
        Question(
            "What is list comprehension in Python?",
            listOf(
                "A technique to summarize list contents",
                "A shorthand way to create lists",
                "A validation method for lists",
                "A way to compress list data"
            ),
            1
        )
    )

    fun getQuestionsByTopic(topic: String): List<Question> {
        return when (topic.lowercase()) {
            "python" -> pythonQuestions
            else -> emptyList()
        }
    }

    fun getRandomQuestion(topic: String): Question? {
        val questions = getQuestionsByTopic(topic)
        if (questions.isEmpty()) return null

        val randomIndex = kotlin.random.Random.nextInt(questions.size)
        return questions[randomIndex]
    }

    fun checkAnswer(question: Question, selectedAnswerIndex: Int): Boolean {
        return selectedAnswerIndex == question.correctAnswerIndex
    }


    fun startPopupService(topic: String, activity: String, intervalMs: Long, testMode: Boolean = false) {
        val intent = Intent(context, PopupService::class.java).apply {
            putExtra("topic", topic)
            putExtra("activity", activity)
            putExtra("interval", intervalMs)
            putExtra("test_mode", testMode)
            action = "START_SERVICE"
        }
        context.startService(intent)
    }

    fun stopPopupService() {
        val intent = Intent(context, PopupService::class.java).apply {
            action = "STOP_SERVICE"
        }
        context.startService(intent)
    }

    fun awardGears(amount: Int = 1): Boolean {
        val user = userController.getCurrentUser() ?: return false
        return userController.updateGears(user.gears + amount)
    }
}