package com.tnyx.data.profile

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InternalProfileAvatarStore internal constructor(
    private val rootDirectory: File,
) {
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : this(File(context.filesDir, ROOT_DIRECTORY_NAME))

    suspend fun write(userId: String, jpegBytes: ByteArray): String = withContext(Dispatchers.IO) {
        require(jpegBytes.isNotEmpty()) { "Avatar image is empty" }
        require(USER_ID_PATTERN.matches(userId)) { "Avatar user ID is invalid" }

        val userDirectory = File(rootDirectory, userId).apply {
            check(mkdirs() || isDirectory) { "Avatar directory could not be created" }
        }
        val target = File(userDirectory, AVATAR_FILE_NAME)
        val temporary = File.createTempFile("avatar-", ".tmp", userDirectory)

        try {
            temporary.outputStream().use { output -> output.write(jpegBytes) }
            moveReplacing(temporary, target)
            Uri.fromFile(target).toString()
        } finally {
            temporary.delete()
        }
    }

    suspend fun delete(userId: String) = withContext(Dispatchers.IO) {
        require(USER_ID_PATTERN.matches(userId)) { "Avatar user ID is invalid" }

        val userDirectory = File(rootDirectory, userId)
        File(userDirectory, AVATAR_FILE_NAME).delete()
        userDirectory.delete()
        Unit
    }

    internal fun avatarFile(userId: String): File {
        return File(File(rootDirectory, userId), AVATAR_FILE_NAME)
    }

    private fun moveReplacing(source: File, target: File) {
        try {
            Files.move(
                source.toPath(),
                target.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(
                source.toPath(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
    }

    private companion object {
        const val ROOT_DIRECTORY_NAME = "profile_avatars"
        const val AVATAR_FILE_NAME = "avatar.jpg"
        val USER_ID_PATTERN = Regex("[A-Za-z0-9._-]{1,128}")
    }
}
