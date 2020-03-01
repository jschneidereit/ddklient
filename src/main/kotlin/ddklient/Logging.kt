package ddklient

const val LOG_PREFIX = "ddklient info:"
const val ERROR_PREFIX = "ddklient error:"

fun log(message: String) = println("$LOG_PREFIX $message")

fun err(message: String) = System.err.println("$ERROR_PREFIX $message")