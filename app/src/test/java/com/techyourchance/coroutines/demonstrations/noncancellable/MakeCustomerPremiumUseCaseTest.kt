package com.techyourchance.coroutines.demonstrations.noncancellable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.Test

class MakeCustomerPremiumUseCaseTest {

    class EndpointTd : PremiumCustomersEndpoint() {
        override suspend fun makeCustomerPremium(customerId: String): Customer {
            return Customer(customerId, true)
        }
    }

    class DaoTd : CustomersDao() {
        lateinit var lastCustomer: Customer
        override suspend fun updateCustomer(customer: Customer) {
            delay(100)
            lastCustomer = customer
        }
    }

    @Test
    fun verifyNonCancellable() {
        val endpointTd = EndpointTd()
        val daoTd = DaoTd()
        val SUT = MakeCustomerPremiumUseCase(endpointTd, daoTd)
        val scope = CoroutineScope(Dispatchers.Default)
        runBlocking {
            val job1 = scope.launch {
                SUT.makeCustomerPremium("testId")
            }
            val job2 = scope.launch {
                delay(50)
                job1.cancel()
            }
            joinAll(job1, job2)
        }
        MatcherAssert.assertThat(daoTd.lastCustomer, Is.`is`(Customer("testId", true)))
    }
}