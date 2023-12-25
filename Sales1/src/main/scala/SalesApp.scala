import java.sql.{Connection, DriverManager, ResultSet, PreparedStatement}
import java.time.LocalDate
import scala.io.StdIn


object SalesApp {
  def main(args: Array[String]): Unit = {
    // Establishing a connection to the database
    val connection = DriverManager.getConnection("jdbc:mysql://localhost:3307/sales", "root", "root")

    //User authntication
    if (!authenticateUser(connection)){
      println("Authentication failed. Exiting the application.")
      connection.close()
      return
    }
    var choice = 0
    do {
      println("\n--- Sales Application Menu ---")
      println("1. Add a new product")
      println("2. Update the price of an existing product")
      println("3. Delete a product")
      println("4. Exit")
      println("Enter your choice (1-4): ")
      choice = StdIn.readInt()

      choice match {
        case 1 => addProduct(connection)
        case 2 => updateProductPrice(connection)
        case 3 => deleteOldProducts(connection)
        case 4 => println("Exiting the application.")
        case _ => println("Invalid choice. Please try again.")
      }
    } while (choice != 4)

  }



  private def addProduct(connection: Connection): Unit = {
    println("Enter the name of the product:")
    val name = StdIn.readLine()
    println("Enter the price of the product:")
    val price = StdIn.readDouble()
    val statement = connection.prepareStatement("INSERT INTO products (name, price, purchase_date) VALUES (?, ?, ?)")
    statement.setString(1, name)
    statement.setDouble(2, price)
    statement.setObject(3, LocalDate.now())
    statement.executeUpdate()
    statement.close()
  }

  def authenticateUser(connection: Connection) : Boolean = {
    println("Enter you username:")
    val username = StdIn.readLine()
    println("Enter your password:")
    val password = StdIn.readLine()
    val query = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?"
    val statement: PreparedStatement = connection.prepareStatement(query)
    statement.setString(1, username)
    statement.setString(2, password)
    val resultSet: ResultSet = statement.executeQuery()
    resultSet.next()
    val count = resultSet.getInt(1)
    resultSet.close()
    statement.close()

    count == 1
  }

  private def deleteOldProducts(connection: Connection): Unit = {
    val statement = connection.createStatement()
    val resultSet = statement.executeQuery("SELECT * FROM products WHERE purchase_date < DATE_SUB(NOW(), INTERVAL 6 MONTH)")
    while (resultSet.next()) {
      val productId = resultSet.getInt("id")
      deleteProduct(connection, productId)
    }
    resultSet.close()
    statement.close()
    println("Products more than 6 months were deleted.")
  }

  private def deleteProduct(connection: Connection, productId: Int): Unit = {
    val statement = connection.prepareStatement("DELETE FROM products WHERE id = ?")
    statement.setInt(1, productId)
    statement.executeUpdate()
    statement.close()
  }

  private def updateProductPrice(connection: Connection): Unit = {
    println("Enter the name of the product:")
    val name = StdIn.readLine()
    println("Enter the price of the product:")
    val price = StdIn.readDouble()
    val statement = connection.prepareStatement("UPDATE products SET price = ? WHERE name = ?")
    statement.setDouble(1, price)
    statement.setString(2, name)
    statement.executeUpdate()
    statement.close()
  }
}