package io.rsbox.engine.oldplugin

import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.defaultImports

object KotlinPluginConfiguration : ScriptCompilationConfiguration({
    defaultImports(
            "io.rsbox.engine.fs.def.*",

            "io.rsbox.engine.model.*",
            "io.rsbox.engine.model.item.*",
            "io.rsbox.engine.model.entity.*",
            "io.rsbox.engine.model.container.*",
            "io.rsbox.engine.model.container.key.*",
            "io.rsbox.engine.model.queue.*",
            "io.rsbox.api.AttributeKey",
            "io.rsbox.engine.model.timer.TimerKey",
            "io.rsbox.engine.model.shop.ShopItem",
            "io.rsbox.engine.model.shop.PurchasePolicy",
            "io.rsbox.engine.model.shop.StockType",

            "io.rsbox.engine.oldplugin.Plugin",

            "io.rsbox.plugins.api.*",
            "io.rsbox.plugins.api.ext.*",
            "io.rsbox.plugins.api.cfg.*",
            "io.rsbox.plugins.api.dsl.*"
    )
})