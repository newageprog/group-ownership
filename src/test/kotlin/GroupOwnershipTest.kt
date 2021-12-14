import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Order


class GroupOwnershipTest {
    private val g  = GroupOwnership(6)
    private val s1 = SubjectItem("S1", setOf(2,3)) 
    private val s2 = SubjectItem("S2", setOf(2,4,5))
    private val s3 = SubjectItem("S3", setOf(2))
    private val s4 = SubjectItem("S4", setOf(2), true)
    @Test
    @Order(1)
    fun t01addS1() {
        assertEquals(setOf(2 to "S1", 3 to "S1"), g.addSubject(s1))
    }

    @Test
    fun t02addS2(){
        assertEquals(setOf(2 to "S1", 3 to "S1"), g.addSubject(s1))
        assertEquals(setOf(2 to "S1", 3 to "S1", 4 to "S2", 5 to "S2"), g.addSubject(s2))
    }

    @Test
    fun t03addS3(){
        assertEquals(setOf(2 to "S1", 3 to "S1"), g.addSubject(s1))
        assertEquals(setOf(2 to "S1", 3 to "S1", 4 to "S2", 5 to "S2"), g.addSubject(s2))
        assertEquals(setOf(2 to "S3", 3 to "S1", 4 to "S2", 5 to "S2"), g.addSubject(s3))
    }


    @Test
    fun t04removeS2(){
        assertEquals(setOf(2 to "S1", 3 to "S1"), g.addSubject(s1))
        assertEquals(setOf(2 to "S1", 3 to "S1", 4 to "S2", 5 to "S2"), g.addSubject(s2))
        assertEquals(setOf(2 to "S3", 3 to "S1", 4 to "S2", 5 to "S2"), g.addSubject(s3))
        assertEquals(setOf(2 to "S3", 3 to "S1"), g.removeSubject("S2"))
    }

    @Test
    fun t05addS4LowPrio(){
        assertEquals(setOf(2 to "S1", 3 to "S1"),                       g.addSubject(s1))
        assertEquals(setOf(2 to "S1", 3 to "S1", 4 to "S2", 5 to "S2"), g.addSubject(s2))
        assertEquals(setOf(2 to "S3", 3 to "S1", 4 to "S2", 5 to "S2"), g.addSubject(s3))
        assertEquals(setOf(2 to "S3", 3 to "S1"),                       g.removeSubject("S2"))
        assertEquals(setOf(2 to "S3", 3 to "S1"),                       g.addSubject(s4))
    }

}
