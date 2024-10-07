import java.io.{BufferedReader, BufferedWriter, InputStreamReader, OutputStreamWriter, PrintWriter}
import java.net.{ServerSocket, Socket}
import scala.collection.mutable
import scala.util.control.Breaks.break

object IRCServer {
  def main(args: Array[String]): Unit = {
    val server = "127.0.0.1"
    val port = 6663

    val serverSocket = new ServerSocket(port)
    println(s"IRC chat is running on port $port")

    val clients = mutable.Map[String, PrintWriter]()

    while (true) {
      val clientSocket = serverSocket.accept()
      println("New client connected!!")
      new Thread(new ClientHandler(clientSocket, clients)).start()
    }
  }

  class ClientHandler(val clientSocket : Socket,
                      val clients : mutable.Map[String, PrintWriter]) extends Runnable {

    private val in : BufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream))
    private val out: PrintWriter = new PrintWriter(clientSocket.getOutputStream, true)
    private var nickname: String = ""

    override def run(): Unit = {
      try {
        out.println("Please write your nickname")
        nickname = in.readLine();
        clients += (nickname -> out)
        broadcast(s"$nickname joined to chat!", out)

        var message: String = ""

        while ({message = in.readLine(); message != null}) {
          if(message == "/quit") {
            break
          } else {
            broadcast(message, out)
          }
        }
      } catch {
        case e: Exception =>
          e.printStackTrace()
      } finally {
        clients-= nickname
        in.close()
        broadcast(s"$nickname has leaved chat", out)
        out.close()
        clientSocket.close()
      }
    }

    private def broadcast(message: String, writer: PrintWriter) : Unit= {
      clients.values.foreach(writer => writer.println(message))
    }
  }
}
