package co.vulpin.birthday.db

import co.vulpin.birthday.db.entities.Guild
import co.vulpin.birthday.db.entities.User
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions

@Singleton(strict = false)
class Database {

    @Delegate
    private Firestore firestore

    Database() {
        def creds = GoogleCredentials.applicationDefault

        def opts = FirestoreOptions.newBuilder()
                .setCredentials(creds)
                .setTimestampsInSnapshotsEnabled(true)
                .build()

        firestore = opts.service
    }

    DocumentReference getGuildRef(String guildId) {
        return collection("guilds").document(guildId)
    }

    Guild getGuild(String guildId) {
        return getGuildRef(guildId).get().get()?.toObject(Guild) ?: new Guild()
    }

    DocumentReference getUserRef(String userId) {
        return collection("users").document(userId)
    }

    User getUser(String userId) {
        return getUserRef(userId).get().get()?.toObject(User) ?: new User()
    }

}
