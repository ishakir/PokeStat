package db

object Queries {

  object Generation {

    def insert(number: Byte)          = "INSERT INTO generations (number) VALUES ("+number+");"
    def update(id: Int, number: Byte) = "UPDATE generations SET number="+number+" WHERE id="+id+";"
    def getAll()                      = "SELECT * FROM generations;"
    def getByNumber(number: Byte)     = "SELECT * FROM generations WHERE number="+number+";"
    def getById(id: Int)              = "SELECT * FROM generations WHERE id="+id+";"
    def deleteById(id: Int)           = "DELETE FROM generations WHERE id="+id+";"

  }

}