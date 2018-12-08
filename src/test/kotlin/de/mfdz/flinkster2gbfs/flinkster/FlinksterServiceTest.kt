package de.mfdz.flinkster2gbfs.flinkster

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlinksterServiceTest {

    @Test
    fun testGetArea(){
        val flinksterService = FlinksterService(System.getenv("FLINKSTER_TOKEN"))
        val areas = flinksterService.getAreas(2, type="station", provider = "regiorad_stuttgart")
        Assertions.assertTrue(areas.size > 50, "Retrieved less than expected stations")
        println(areas)
    }


}