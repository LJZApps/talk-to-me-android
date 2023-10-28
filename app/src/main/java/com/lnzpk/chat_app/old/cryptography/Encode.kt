package com.lnzpk.chat_app.old.cryptography

object Encode {
    /**
     * Verschlüsselt den String
     *
     * @param s
     * @return
     */
    fun encode(s: String): String {
        // create a string to add in the initial
        // binary code for extra security
        val ini = "11111111"
        var cu = 0

        // create an array
        val arr = IntArray(11111111)

        // iterate through the string
        for (i in s.indices) {
            // put the ascii value of
            // each character in the array
            arr[i] = s[i].code
            cu++
        }
        var res = ""

        // create another array
        val bin = IntArray(111)
        var idx = 0

        // run a loop of the size of string
        for (i1 in 0 until cu) {

            // get the ascii value at position
            // i1 from the first array
            var temp = arr[i1]

            // run the second nested loop of same size
            // and set 0 value in the second array
            for (j in 0 until cu) bin[j] = 0
            idx = 0

            // run a while for temp > 0
            while (temp > 0) {
                // store the temp module
                // of 2 in the 2nd array
                bin[idx++] = temp % 2
                temp /= 2
            }
            var dig = ""
            var temps: String

            // run a loop of size 7
            for (j in 0..6) {

                // convert the integer to string
                temps = bin[j].toString()

                // add the string using
                // concatenation function
                dig += temps
            }
            var revs = ""

            // reverse the string
            for (j in dig.length - 1 downTo 0) {
                val ca = dig[j]
                revs += ca.toString()
            }
            res += revs
        }
        // add the extra string to the binary code
        res = ini + res

        // return the encrypted code
        return res
    }
}