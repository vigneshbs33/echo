package com.echo.app.field

import com.echo.app.service.EchoAccessibilityService
import com.echo.core.domain.field.FieldSnapshot
import com.echo.core.domain.field.TextFieldGateway

/** Reaches the focused text field of any app through [EchoAccessibilityService]. */
object AccessibilityTextFieldGateway : TextFieldGateway {

    override val isAvailable: Boolean
        get() = EchoAccessibilityService.instance != null

    override fun read(): FieldSnapshot? = EchoAccessibilityService.instance?.readFocusedField()

    override fun write(snapshot: FieldSnapshot): Boolean =
        EchoAccessibilityService.instance?.writeFocusedField(snapshot) ?: false
}
