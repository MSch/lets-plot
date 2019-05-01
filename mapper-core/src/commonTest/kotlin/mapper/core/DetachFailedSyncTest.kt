package jetbrains.datalore.mapper.core

import jetbrains.datalore.base.observable.collections.list.ObservableArrayList
import jetbrains.datalore.mapper.core.Synchronizers.forObservableRole
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.fail

class DetachFailedSyncTest {
    private val mySource = ObservableArrayList<String>()
    private lateinit var myRoot: Mapper<Any, Any>

    @BeforeTest
    fun setup() {
        myRoot = object : Mapper<Any, Any>(Any(), Any()) {
            override fun registerSynchronizers(conf: SynchronizersConfiguration) {
                conf.add(forObservableRole(this, mySource, ArrayList(), object : MapperFactory<String, String> {
                    override fun createMapper(source: String): Mapper<out String, out String> {
                        throw RuntimeException()
                    }
                }))
            }
        }
    }

    @Test
    fun detachFailedSync() {
        mySource.add("any")
        try {
            myRoot.attachRoot()
            fail("Exception expected")
        } catch (ignored: RuntimeException) {
        }

        myRoot.detachRoot()
        assertFalse(myRoot.isAttached)
    }
}
