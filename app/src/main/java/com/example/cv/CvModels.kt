package com.example.cv

import android.graphics.Bitmap

/**
 * Image Quality Validation Result
 */
data class ImageQualityResult(
    val isPassed: Boolean,
    val blurScore: Double = 0.0,          // Laplacian variance (higher = sharper)
    val brightnessScore: Double = 0.0,    // Mean luminance (0 - 255)
    val contrastScore: Double = 0.0,      // Std dev of luminance
    val centerAlignmentScore: Double = 0.0,// 0.0 - 1.0 (closeness to center)
    val overallScore: Double = 0.0,       // 0 - 100%
    val rejectionReason: String? = null,
    val isClassifiable: Boolean = true,
    val sampleCoveragePct: Double = 100.0
) {
    val sampleCoverage: Double get() = sampleCoveragePct
    val qualityPassed: Int get() = if (isPassed) 1 else 0
}

/**
 * 3D Median LAB coordinates
 */
data class MedianLab(
    val l: Double = 0.0,
    val a: Double = 0.0,
    val b: Double = 0.0
) : Comparable<MedianLab> {
    val medianL: Double get() = l
    val medianA: Double get() = a
    val medianB: Double get() = b

    override operator fun compareTo(other: MedianLab): Int {
        val cmpL = l.compareTo(other.l)
        if (cmpL != 0) return cmpL
        val cmpA = a.compareTo(other.a)
        if (cmpA != 0) return cmpA
        return b.compareTo(other.b)
    }
}
typealias LabMedian = MedianLab

/**
 * Reference Sample in LAB space with histograms
 */
data class ReferenceSampleLab(
    val id: Int = 0,
    val name: String = "",
    val actualIcumsa: Double = 0.0,
    val medianLab: MedianLab = MedianLab(),
    val vector191: DoubleArray = DoubleArray(191),
    val rgbHistogram64: DoubleArray = DoubleArray(64),
    val labHistogram64: DoubleArray = DoubleArray(64)
) : Comparable<ReferenceSampleLab> {
    override operator fun compareTo(other: ReferenceSampleLab): Int {
        return actualIcumsa.compareTo(other.actualIcumsa)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ReferenceSampleLab
        return id == other.id
    }

    override fun hashCode(): Int = id
}

/**
 * 3D Segment Interpolation Result
 */
data class InterpolationSegmentResult(
    val segmentIndex: Int = 0,
    val ref1Id: Int = 0,
    val ref1Name: String = "",
    val ref1Icumsa: Double = 0.0,
    val ref1Lab: MedianLab = MedianLab(),
    val ref2Id: Int = 0,
    val ref2Name: String = "",
    val ref2Icumsa: Double = 0.0,
    val ref2Lab: MedianLab = MedianLab(),
    val rawT: Double = 0.0,
    val clampedT: Double = 0.0,
    val projectedLab: MedianLab = MedianLab(),
    val perpendicularDistance: Double = 0.0,
    val isOutOfBounds: Boolean = false,
    val warningMessage: String? = null
)

/**
 * Segment Detail for Developer Center
 */
data class InterpolationSegmentDetail(
    val isSelected: Boolean = false,
    val segmentIndex: Int = 0,
    val ref1: ReferenceMatch = ReferenceMatch(1, "Ref 1", 45),
    val ref2: ReferenceMatch = ReferenceMatch(2, "Ref 2", 85),
    val rawT: Double = 0.0,
    val clampedT: Double = 0.0,
    val nearestPoint: MedianLab = MedianLab(),
    val distance: Double = 0.0
)

/**
 * High-level ICUMSA Estimation Result used in UI and Reports
 */
data class IcumsaEstimationResult(
    val estimatedIcumsa: Double = 0.0,
    val isOutOfRange: Boolean = false,
    val outOfRangeReason: String? = null,
    val isUnknown: Boolean = false,
    val distanceToPath: Double = 0.0,
    val selectedRef1: ReferenceMatch = ReferenceMatch(1, "Ref 1", 45),
    val selectedRef2: ReferenceMatch = ReferenceMatch(2, "Ref 2", 85),
    val relativePositionT: Double = 0.0,
    val sampleLabMedian: MedianLab = MedianLab(),
    val allSegments: List<InterpolationSegmentDetail> = emptyList()
)

/**
 * Feature vector representation for Single ROI
 */
data class FeatureVector(
    val regionIndex: Int,
    val regionName: String,
    val values: DoubleArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FeatureVector
        if (regionIndex != other.regionIndex) return false
        if (regionName != other.regionName) return false
        if (!values.contentEquals(other.values)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = regionIndex
        result = 31 * result + regionName.hashCode()
        result = 31 * result + values.contentHashCode()
        return result
    }
}

/**
 * Extracted color and statistical feature set for Single ROI (Whole Image)
 */
data class CompleteFeatureSet(
    val regions: List<FeatureVector> = emptyList(),
    val concatenatedVector: DoubleArray = DoubleArray(0),
    val extractionTimeMs: Long = 0L,
    val colorHistogram: FloatArray = FloatArray(0),
    val rgbMeans: Triple<Double, Double, Double> = Triple(0.0, 0.0, 0.0),
    val hsvMeans: Triple<Double, Double, Double> = Triple(0.0, 0.0, 0.0),
    val labMeans: Triple<Double, Double, Double> = Triple(0.0, 0.0, 0.0),
    val labMedian: MedianLab = MedianLab()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CompleteFeatureSet
        return concatenatedVector.contentEquals(other.concatenatedVector)
    }

    override fun hashCode(): Int {
        return concatenatedVector.contentHashCode()
    }
}

/**
 * Individual metric match result against a reference sample
 */
data class ReferenceMatch(
    val sampleId: Int,
    val sampleName: String,
    val icumsaValue: Double,
    val deltaE00: Double = 0.0,
    val medianLab: MedianLab = MedianLab(),
    val euclideanDistance: Double = 0.0,
    val euclideanSimilarityPct: Double = 0.0,
    val cosineSimilarity: Double = 0.0,
    val cosineSimilarityPct: Double = 0.0,
    val bhattacharyyaDistance: Double = 0.0,
    val bhattacharyyaSimilarityPct: Double = 0.0,
    val finalSimilarityPct: Double = 0.0,
    val source: String = "USER",
    val isSynthetic: Boolean = false
) {
    constructor(
        sampleId: Int,
        sampleName: String,
        icumsaValue: Int,
        deltaE00: Double = 0.0,
        medianLab: MedianLab = MedianLab(),
        euclideanDistance: Double = 0.0,
        euclideanSimilarityPct: Double = 0.0,
        cosineSimilarity: Double = 0.0,
        cosineSimilarityPct: Double = 0.0,
        bhattacharyyaDistance: Double = 0.0,
        bhattacharyyaSimilarityPct: Double = 0.0,
        finalSimilarityPct: Double = 0.0,
        source: String = "USER",
        isSynthetic: Boolean = false
    ) : this(
        sampleId = sampleId,
        sampleName = sampleName,
        icumsaValue = icumsaValue.toDouble(),
        deltaE00 = deltaE00,
        medianLab = medianLab,
        euclideanDistance = euclideanDistance,
        euclideanSimilarityPct = euclideanSimilarityPct,
        cosineSimilarity = cosineSimilarity,
        cosineSimilarityPct = cosineSimilarityPct,
        bhattacharyyaDistance = bhattacharyyaDistance,
        bhattacharyyaSimilarityPct = bhattacharyyaSimilarityPct,
        finalSimilarityPct = finalSimilarityPct,
        source = source,
        isSynthetic = isSynthetic
    )
}

/**
 * Confidence Level Enum
 */
enum class ConfidenceLevel {
    VERY_HIGH,
    HIGH,
    MEDIUM,
    LOW
}

/**
 * Final Classification Result
 */
data class ClassificationResult(
    val nearestSampleId: Int,
    val nearestSampleName: String,
    val isUnknown: Boolean,
    val confidenceScorePct: Double,
    val confidenceLevel: ConfidenceLevel,
    val topMatch: ReferenceMatch,
    val allMatches: List<ReferenceMatch>,
    val processingTimeMs: Long,
    val thresholdUsed: Double,
    val marginPct: Double = 0.0,
    val isClassifiable: Boolean = true,
    val estimatedIcumsa: Double = 0.0,
    val estimationLabel: String = "",
    val sampleMedianLab: MedianLab = MedianLab(),
    val interpolation: InterpolationSegmentResult? = null,
    val qualityResult: ImageQualityResult? = null,
    val validPixelCount: Int = 0,
    val totalMaskPixelCount: Int = 0,
    val sampleCoveragePct: Double = 0.0,
    val estimatedReferenceLevel: Double? = null,
    val isSyntheticDemo: Boolean = false,
    val icumsaEstimation: IcumsaEstimationResult? = null
) {
    val sampleCoverage: Double get() = sampleCoveragePct
}

/**
 * Debug Stage Bitmap representation for Developer Mode
 */
data class ProcessingStage(
    val stageName: String,
    val description: String,
    val bitmap: Bitmap
)

/**
 * Two-sample direct comparison state
 */
data class TwoSampleComparisonState(
    val bitmapA: Bitmap? = null,
    val bitmapB: Bitmap? = null,
    val isComparing: Boolean = false,
    val errorMessage: String? = null,
    val diffResult: Ciede2000.DiffResult? = null,
    val profileA: ColorStats.MaskedColorProfile? = null,
    val profileB: ColorStats.MaskedColorProfile? = null
)
