import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.Socket

object IRCClient {
  def main(unit: Array[String]): Unit = {
    val server = "127.0.0.1"
    val port = 6663
    try {
      val bufferedReader: BufferedReader = new BufferedReader(new InputStreamReader(System.in))
      println("Please write your nickname:")
      val nickname = bufferedReader.readLine()

      val clientSocket = new Socket(server, port)
      val in: BufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream))
      val out: PrintWriter = new PrintWriter(clientSocket.getOutputStream, true)

      out.println(s"NICK $nickname")
      out.println(s"USER $nickname 0 * :$nickname")

      new Thread(new Runnable {
        override def run(): Unit = {
          var message = ""
          while ({ message = in.readLine(); message != null }) {
            println(message)
            if (message.startsWith("PING")) {
              out.println(s"PONG ${message.substring(5)}")
            }
          }
        }
      }).start()

      while (true) {
        val userInput = scala.io.StdIn.readLine()
        if (userInput.startsWith("/join ")) {
          val channel = userInput.substring(6).trim
          out.println(s"JOIN $channel")
        } else {
          out.println(s"PRIVMSG SCALA-chat :$userInput")
        }
        out.flush()
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }
}
