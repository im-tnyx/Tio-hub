package com.tnyx.core.legal.presentation.action

sealed class LegalAction {
    data object CloseTapped : LegalAction()
    data object BackdropTapped : LegalAction()
    data object RetryTapped : LegalAction()
}
