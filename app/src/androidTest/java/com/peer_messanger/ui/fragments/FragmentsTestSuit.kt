package com.peer_messanger.ui.fragments

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.runner.RunWith
import org.junit.runners.Suite

@ExperimentalCoroutinesApi
@RunWith(Suite::class)
@Suite.SuiteClasses(HomeFragmentTest::class, FindingFragmentTest::class, ChatFragmentTest::class)
class FragmentsTestSuit
