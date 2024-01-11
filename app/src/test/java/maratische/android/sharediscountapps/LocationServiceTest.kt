package maratische.android.sharediscountapps

import org.junit.Assert.assertEquals
import org.junit.Test

class LocationServiceTest {

    @Test fun emailValidator_CorrectEmailSimple_ReturnsTrue() {
        val locationService = LocationService()
        val temp = locationService.calcTemp(1000*60*2, 1, 0.2)
        val speed = locationService.calcSpeed(1000*60*2, 1, 0.2)
        println("a {it.accuracy} d distance s $speed $temp (lat, long)")
        assertEquals(2.0, temp)
    }

}