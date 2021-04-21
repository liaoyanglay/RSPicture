#pragma version(1)
#pragma rs java_package_name(com.dizzylay.rspicture.rs)
#pragma rs_fp_relaxed

#include "Clamp.rsh"

// The brightness factor.
// Should be in the range [-1.0f, 1.0f].
float BrightnessFactor = 0;

uchar4 RS_KERNEL brightness(uchar4 in) {
    float4 f4 = rsUnpackColor8888(in);
    float3 f3 = f4.rgb;

	// Modify brightness (addition)
	if (BrightnessFactor != 0.0f) {
	   	// Add brightness
	   	f3 = f3 + BrightnessFactor;
	   	f3 = FClamp01Float3(f3);
	}

	return rsPackColorTo8888(f3);
}

// The contrast factor.
// Should be in the range [-1.0f, 1.0f].
float ContrastFactor = 0;

// magic factor
static float ContrastFactor1;

uchar4 RS_KERNEL contrastProcess(uchar4 in) {
	float4 f4 = rsUnpackColor8888(in);
    float3 f3 = f4.rgb;

	// Modifiy contrast (multiplication)
 	if (ContrastFactor1 != 1.0f){
	    // Transform to range [-0.5f, 0.5f]
	    f3 = f3 - 0.5f;
	    // Multiply contrast factor
	    f3 = f3 * ContrastFactor1;
	    // Transform back to range [0.0f, 1.0f]
	    f3 = f3 + 0.5f;
	    f3 = FClamp01Float3(f3);
	}

    return rsPackColorTo8888(f3);
}

void contrast(rs_allocation in, rs_allocation out) {
	ContrastFactor1  = (1.0f + ContrastFactor) * (1.0f + ContrastFactor);
    rsForEach(contrastProcess, in, out);
}

static const float3 grayWeight = {0.299f, 0.587f, 0.114f};

float SaturationFactor = 1.f;

// saturation manipulation.
uchar4 RS_KERNEL saturation(uchar4 in) {
    float4 f4 = rsUnpackColor8888(in);
    float3 result = dot(f4.rgb, grayWeight);
    result = mix(result, f4.rgb, SaturationFactor);

    return rsPackColorTo8888(result);
}

void enhance(rs_allocation in, rs_allocation out) {
    const uint32_t width = rsAllocationGetDimX(in);
    const uint32_t height = rsAllocationGetDimY(in);
    rs_allocation tmp1 = rsCreateAllocation_uchar4(width, height);
    rs_allocation tmp2 = rsCreateAllocation_uchar4(width, height);
	ContrastFactor1  = (1.0f + ContrastFactor) * (1.0f + ContrastFactor);
    rsForEach(contrastProcess, in, tmp1);
    rsForEach(saturation, tmp1, tmp2);
    rsForEach(brightness, tmp2, out);
}
