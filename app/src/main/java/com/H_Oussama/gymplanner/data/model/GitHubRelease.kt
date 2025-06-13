package com.H_Oussama.gymplanner.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GitHubRelease(
    val tag_name: String,
    val name: String,
    val body: String,
    val assets: List<ReleaseAsset>
)

@Serializable
data class ReleaseAsset(
    val browser_download_url: String,
    val name: String
) 