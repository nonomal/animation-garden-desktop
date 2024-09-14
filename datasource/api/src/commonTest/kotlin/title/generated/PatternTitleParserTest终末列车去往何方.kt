/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

// @formatter:off
@file:Suppress(
  "FunctionName",
  "ClassName",
  "RedundantVisibilityModifier",
  "PackageDirectoryMismatch",
  "NonAsciiCharacters",
  "SpellCheckingInspection",
)

import me.him188.ani.datasources.api.SubtitleKind
import me.him188.ani.datasources.api.title.PatternBasedTitleParserTestSuite
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 原名: `终末列车去往何方`
 * 数据源: `dmhy`
 *
 * 由 `test-codegen` 的 `GenerateTests.kt` 生成, 不要手动修改!
 * 如果你优化了解析器, 这些 test 可能会失败, 请检查是否它是因为以前解析错误而现在解析正确了. 
 * 如果是, 请更新测试数据: 执行 `GenerateTests.kt`.
 */
public class PatternTitleParserTest终末列车去往何方 : PatternBasedTitleParserTestSuite() {
  @Test
  public fun `670442_Shuumatsu_Train_Doko_e_Iku_08_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 終末列車去往何方?/ 末班列車去哪裡?/ Shuumatsu Train Doko e Iku [08][WebRip][HEVC_AAC][繁體內嵌]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `670441_Shuumatsu_Train_Doko_e_Iku_08_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 终末列车去往何方?/ 末班列车去哪里?/ Shuumatsu Train Doko e Iku [08][WebRip][HEVC_AAC][简体内嵌]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `670440_Shuumatsu_Train_Doko_e_Iku_08_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 终末列车去往何方?/ 末班列车去哪里?/ Shuumatsu Train Doko e Iku [08][WebRip][HEVC_AAC][简繁内封]")
    assertEquals("08..08", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `669853_Shuumatsu_Train_Doko_e_Iku_07_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 终末列车去往何方?/ 末班列车去哪里?/ Shuumatsu Train Doko e Iku [07][WebRip][HEVC_AAC][简繁内封]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `669852_Shuumatsu_Train_Doko_e_Iku_07_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 終末列車去往何方?/ 末班列車去哪裡?/ Shuumatsu Train Doko e Iku [07][WebRip][HEVC_AAC][繁體內嵌]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `669851_Shuumatsu_Train_Doko_e_Iku_07_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 终末列车去往何方?/ 末班列车去哪里?/ Shuumatsu Train Doko e Iku [07][WebRip][HEVC_AAC][简体内嵌]")
    assertEquals("07..07", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `669084_Shuumatsu_Train_Doko_e_Iku_06_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 终末列车去往何方?/ 末班列车去哪里?/ Shuumatsu Train Doko e Iku [06][WebRip][HEVC_AAC][简繁内封]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `669083_Shuumatsu_Train_Doko_e_Iku_06_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 终末列车去往何方?/ 末班列车去哪里?/ Shuumatsu Train Doko e Iku [06][WebRip][HEVC_AAC][简体内嵌]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `669082_Shuumatsu_Train_Doko_e_Iku_06_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 終末列車去往何方?/ 末班列車去哪裡?/ Shuumatsu Train Doko e Iku [06][WebRip][HEVC_AAC][繁體內嵌]")
    assertEquals("06..06", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `668544_Shuumatsu_Train_Doko_e_Iku_05_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 终末列车去往何方?/ 末班列车去哪里?/ Shuumatsu Train Doko e Iku [05][WebRip][HEVC_AAC][简繁内封]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `668543_Shuumatsu_Train_Doko_e_Iku_05_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 终末列车去往何方?/ 末班列车去哪里?/ Shuumatsu Train Doko e Iku [05][WebRip][HEVC_AAC][简体内嵌]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `668542_Shuumatsu_Train_Doko_e_Iku_05_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 終末列車去往何方?/ 末班列車去哪裡?/ Shuumatsu Train Doko e Iku [05][WebRip][HEVC_AAC][繁體內嵌]")
    assertEquals("05..05", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `668092_Shuumatsu_Train_Doko_e_Iku_04_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 終末列車去往何方?/ 末班列車去哪裡?/ Shuumatsu Train Doko e Iku [04][WebRip][HEVC_AAC][繁體內嵌]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `668034_Shuumatsu_Train_Doko_e_Iku_04_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 终末列车去往何方?/ 末班列车去哪里?/ Shuumatsu Train Doko e Iku [04][WebRip][HEVC_AAC][简繁内封]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `668033_Shuumatsu_Train_Doko_e_Iku_04_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 终末列车去往何方?/ 末班列车去哪里?/ Shuumatsu Train Doko e Iku [04][WebRip][HEVC_AAC][简体内嵌]")
    assertEquals("04..04", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `667470_Shuumatsu_Train_Doko_e_Iku_03_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 終末列車去往何方?/ 末班列車去哪裡?/ Shuumatsu Train Doko e Iku [03][WebRip][HEVC_AAC][繁體內嵌]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `667469_Shuumatsu_Train_Doko_e_Iku_03_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 终末列车去往何方?/ 末班列车去哪里?/ Shuumatsu Train Doko e Iku [03][WebRip][HEVC_AAC][简体内嵌]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `667468_Shuumatsu_Train_Doko_e_Iku_03_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 终末列车去往何方?/ 末班列车去哪里?/ Shuumatsu Train Doko e Iku [03][WebRip][HEVC_AAC][简繁内封]")
    assertEquals("03..03", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `666692_Shuumatsu_Train_Doko_e_Iku_02_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 终末列车去往何方?/ 末班列车去哪里?/ Shuumatsu Train Doko e Iku [02][WebRip][HEVC_AAC][简繁内封]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `666691_Shuumatsu_Train_Doko_e_Iku_02_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 終末列車去往何方?/ 末班列車去哪裡?/ Shuumatsu Train Doko e Iku [02][WebRip][HEVC_AAC][繁體內嵌]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `666690_Shuumatsu_Train_Doko_e_Iku_02_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 终末列车去往何方?/ 末班列车去哪里?/ Shuumatsu Train Doko e Iku [02][WebRip][HEVC_AAC][简体内嵌]")
    assertEquals("02..02", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `666212_Shuumatsu_Train_Doko_e_Iku_01v2_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 终末列车去往何方?/ 末班列车去哪里?/ Shuumatsu Train Doko e Iku [01v2][WebRip][HEVC_AAC][简繁内封]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS, CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.CLOSED, r.subtitleKind)
  }

  @Test
  public fun `666211_Shuumatsu_Train_Doko_e_Iku_01v2_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 終末列車去往何方?/ 末班列車去哪裡?/ Shuumatsu Train Doko e Iku [01v2][WebRip][HEVC_AAC][繁體內嵌]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `666210_Shuumatsu_Train_Doko_e_Iku_01v2_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 终末列车去往何方?/ 末班列车去哪里?/ Shuumatsu Train Doko e Iku [01v2][WebRip][HEVC_AAC][简体内嵌]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }

  @Test
  public fun `666067_Shuumatsu_Train_Doko_e_Iku_01_WebRip_HEVC_AAC`() {
    val r =
        parse("[北宇治字幕组] 终末列车去往何方?/ 末班列车去哪里?/ Shuumatsu Train Doko e Iku [01][WebRip][HEVC_AAC][简体内嵌]")
    assertEquals("01..01", r.episodeRange.toString())
    assertEquals("CHS", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
    assertEquals("null", r.resolution.toString())
    assertEquals(SubtitleKind.EMBEDDED, r.subtitleKind)
  }
}

// @formatter:on
