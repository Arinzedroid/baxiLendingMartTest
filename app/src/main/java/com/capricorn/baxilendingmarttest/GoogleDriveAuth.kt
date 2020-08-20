package com.capricorn.baxilendingmarttest

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*


object GoogleDriveAuth{


    val APPLICATION_NAME = "Baxi LendingMart"
    val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
    private val TOKENS_DIRECTORY_PATH = "tokens"


    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */

    private val SCOPES: List<String> =
        Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY)
    private val CREDENTIALS_FILE_PATH = "/credentials.json"


    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */

    @Throws(IOException::class)

    suspend fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential? {

        return withContext(Dispatchers.IO){
            val inStream: InputStream =
                this::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
                    ?: throw FileNotFoundException("Resource not found: $CREDENTIALS_FILE_PATH")
            val clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inStream))



            // Build flow and trigger user authorization request.
            val flow = GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT,
                JSON_FACTORY, clientSecrets, SCOPES)
                /*.setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))*/
                .setAccessType("offline")
                .build()
            val receiver: LocalServerReceiver = LocalServerReceiver.Builder().setPort(8888).build()
             AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
        }
        // Load client secrets.

    }
}
