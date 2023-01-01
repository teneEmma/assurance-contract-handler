package com.kod.assurancecontracthandler.common.usecases

enum class ContactAction(val action: Int) {
    CALL(0), SMS(1), WHATSAPP_CALL(3), WHATSAPP_TEXT(4)
}