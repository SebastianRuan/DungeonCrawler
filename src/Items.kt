data class Gold(val amt: Int, var dragon:Dragon? = null):Piece('G'){
    override fun toString(): String {
        return super.toString()
    }
}

data class Potion(val kind: PotionType, val amt: Int):Piece('P'){
    override fun toString(): String {
        return super.toString()
    }
}

enum class PotionType{
    H, // health affecting potion
    A, // attack affecting potion
    D  // defence affecting potion
}