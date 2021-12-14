@file:Suppress("UNCHECKED_CAST")

class ObjectItem(public val index: Int, public val name: String = "N$index") {
    public var owner: SubjectItem? = null
}

class SubjectItem(public val name: String, public val ownAllowed: Set<Int>, public val lowPrio: Boolean = false) {
}

class GroupOwnership(val n: Int) {
    private val objectItemList = (0 until n).map { ObjectItem(it) }
    private var subjectItemtList = arrayListOf<SubjectItem>()

    private fun getSubjByName(name: String): SubjectItem = subjectItemtList.filter { it.name == name }.first()

    public fun getOwnSet(): Set<Pair<Int, String>> =
        objectItemList
            .mapIndexed { index, objectItem -> index to objectItem.owner }
            .filter { it.second != null }
            .map { it.first to it.second?.name }
            .toSet() as Set<Pair<Int, String>>

    private fun setOwner(index: Int, subj: SubjectItem) {
//        println("Objects[$index] = ${subj.name}")
        objectItemList[index].owner = subj

    }

    private fun combineOwnSet(newAssign: Set<Pair<Int, String>>): Set<Pair<Int, String>> {
        var resSet = mutableSetOf<Pair<Int, String>>()
        val curOwn = getOwnSet()
        for (i in curOwn) {
            val f = newAssign.filter { it.first == i.first }.count() == 0
            if (f) {
                resSet.add(i)
            }
        }
        resSet.addAll(newAssign)
        return resSet
    }

    private fun getMayOwnSet(index: Int): Set<String> {
        var r = mutableSetOf<String>()
        for (i in subjectItemtList) {
            if (i.ownAllowed.contains(index))
                r.add(i.name)
        }
//        println("Obj[$index] may own $r")
        return r
    }

    // назначаем владельцев в обектах, которые допускают вариацию
    public fun reassignObjects(objList: List<Int>) {
        if (objList.isEmpty())
            return
//        println("reassign elements $objList")


        var listToIterate: MutableList<Pair<Int, List<String>>> = mutableListOf()
        for (i in objList) {
            val l = getMayOwnSet(i).toList()
            if (l.isNotEmpty())
                listToIterate.add(i to l)
        }
        if (listToIterate.isEmpty())
            return
//        println("Result list to iterate = ${listToIterate.toString()}")
        var minMetric = metric(getOwnSet())
        var assignSet = setOf<Pair<Int, String>>()
        if (listToIterate.size > 2)
            nAryCartesianProduct(listToIterate.map { it.second })
                .map { objList.zip(it) }
                .forEach {
                    val m = metric(combineOwnSet(it.toSet() as Set<Pair<Int, String>>))
                    if (m < minMetric) {
                        minMetric = m
                        assignSet = it.toSet() as Set<Pair<Int, String>>
                    }
                }
        else
            listToIterate[0].second
                .forEach { elem ->
                    val a = listToIterate[0].first to elem
                    val s = combineOwnSet(setOf(a))
                    val m = metric(s)
                    if (m <= minMetric) {
                        minMetric = m
                        assignSet = setOf(listToIterate[0].first to elem)
                    }

                }
//        println("minMetric = $minMetric")
        assignSet.forEach {
            setOwner(it.first, getSubjByName(it.second))
        }
//        println("State = ")
        objectItemList
            .filter { it.owner != null }
            .forEach {
//                println("${it.index} -> ${it.owner?.name}")
            }
    }

    fun addSubject(subj: SubjectItem): Set<Pair<Int, String>> {
//        println("addSubject: ${subj.name}")
        //Владеем незанятыми объектами
        val assignRightNowList = subj.ownAllowed
            .filter { objectItemList[it].owner == null }

        assignRightNowList.forEach { setOwner(it, subj) }
        subjectItemtList.add(subj)

        //Владеем объектами, если до этого был lowPrio, а текущий subj имеет lowPrio=false
        if (!subj.lowPrio) {
            subj.ownAllowed
                .filter { objectItemList[it].owner?.lowPrio ?: false }
                .forEach { setOwner(it, subj) }
        } else {
            // отдельный трек для lowPrio
            reassignObjects(subj.ownAllowed
                .filter {
                    ((!assignRightNowList.contains(it)) and (objectItemList[it].owner?.lowPrio == true))
                }
            )
            return getOwnSet()
        }

        //выбираем только занятые объекты, но не в текущей итерации субъектом subj
        val r1 = subj.ownAllowed
            .filter { (objectItemList[it].owner != null) and (objectItemList[it].owner != subj) }

        reassignObjects(r1)
        return getOwnSet()
    }

    fun removeSubject(itemName: String): Set<Pair<Int, String>> {
//        println("removeSubject: ${itemName}")
        val r1 = subjectItemtList.find { it.name == itemName }
        subjectItemtList.remove(r1)
        var objectsOwn = objectItemList
            .filter { it.owner?.name == itemName }
            .map { it.index }
        objectItemList.forEach { if (it.owner?.name == itemName) it.owner = null }
        reassignObjects(objectsOwn)
        return getOwnSet()
    }

    private var metric: (Set<Pair<Int, String>>) -> Int = ::getMetricSumMaxMinusCur

    // Метрика "несправедливости". Пытаемся минимизировать метрику.
    // считаем сумму (максимальног@TestMethodOrder(OrderAnnotation)о количества объектов во владении)-(количество владеемых объектов i-того  субекта)
    // чем меньше метрика, тем "справедливее" распределения владений объектов по субъектам
    fun getMetricSumMaxMinusCur(_ownList: Set<Pair<Int, String>>): Int {
        val ownCounts = _ownList.groupBy { it.second }.map { it.key to it.value.size }
        val maxOwn = ownCounts.map { it.second }.maxOrNull()!!

        val res1 = subjectItemtList
            .map { subjElem ->
                maxOwn - objectItemList.filter { item -> item.owner == subjElem }.count()
            }
        return res1.fold(0) { acc, p -> acc + p }
    }


}


fun flattenList(nestList: List<Any>): List<Any> {
    val flatList = mutableListOf<Any>()

    fun flatten(list: List<Any>) {
        for (e in list) {
            if (e !is List<*>)
                flatList.add(e)
            else
                flatten(e as List<Any>)
        }
    }

    flatten(nestList)
    return flatList
}

operator fun List<Any>.times(other: List<Any>): List<List<Any>> {
    val prod = mutableListOf<List<Any>>()
    for (e in this) {
        for (f in other) {
            prod.add(listOf(e, f))
        }
    }
    return prod
}

fun nAryCartesianProduct(lists: List<List<Any>>): List<List<Any>> {
    require(lists.size >= 2)
    return lists.drop(2).fold(lists[0] * lists[1]) { cp, ls -> cp * ls }.map { flattenList(it) }
}


