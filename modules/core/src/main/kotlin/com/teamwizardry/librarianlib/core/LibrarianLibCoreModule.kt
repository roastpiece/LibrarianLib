package com.teamwizardry.librarianlib.core

import com.teamwizardry.librarianlib.LibrarianLibModule
import org.apache.logging.log4j.LogManager

object LibrarianLibCoreModule : LibrarianLibModule("core", "Core")

internal val logger = LibrarianLibCoreModule.makeLogger(null)
