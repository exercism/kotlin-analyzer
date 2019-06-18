fun twofer(name: String="you"): String {
    if(name != null) {
        return "One for $name, one for me."
    } else {
        return "One for you, one for me."
    }
}