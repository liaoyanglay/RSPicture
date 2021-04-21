#pragma version(1)
#pragma rs java_package_name(com.dizzylay.rspicture.rs)
#pragma rs_fp_relaxed

static const float3 grayWeight = {0.299f, 0.587f, 0.114f};

void init() {
}

void root(const uchar4 *in, uchar4 *out, uint32_t x, uint32_t y) {
}

// 灰度变换
uchar4 RS_KERNEL grayscale(uchar4 in) {
    float4 f4 = rsUnpackColor8888(in);
    float3 result = dot(f4.rgb, grayWeight);

    return rsPackColorTo8888(result);
}

// 黑金色转换
uchar4 RS_KERNEL blackGold(uchar4 in) {
    uchar4 out = in;

    if ((in.r < in.b) && (in.g < in.b)) {
        out.r = out.g = out.b = (in.r * 299 + in.g * 587 + in.b * 114 + 500) / 1000;
    }

    return out;
}

// 色彩反转
uchar4 RS_KERNEL invert(uchar4 in) {
    uchar4 out = in;
    out.r = 255 - in.r;
    out.g = 255 - in.g;
    out.b = 255 - in.b;
    return out;
}

// 怀旧
uchar4 RS_KERNEL nostalgia(uchar4 in) {
    int R = 0.393 * in.r + 0.769 * in.g + 0.189 * in.b;
    int G = 0.349 * in.r + 0.686 * in.g + 0.168 * in.b;
    int B = 0.272 * in.r + 0.534 * in.g + 0.131 * in.b;
    uchar4 out;
    out.r = R < 255 ? R : 255;
    out.g = G < 255 ? G : 255;
    out.b = B < 255 ? B : 255;
    return out;
}

// 连环画
uchar4 RS_KERNEL comic(uchar4 in) {
    int R = abs(in.g - in.b + in.g + in.r) * in.r / 256;
    int G = abs(in.b - in.g + in.b + in.r) * in.r / 256;
    int B = abs(in.b - in.g + in.b + in.r) * in.g / 256;
    uchar4 out;
    out.r = R < 255 ? R : 255;
    out.g = G < 255 ? G : 255;
    out.b = B < 255 ? B : 255;
    return out;
}
