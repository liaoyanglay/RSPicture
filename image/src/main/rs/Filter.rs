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

static float calWeight(float H, float baseH, float range) {
    float delta = H - baseH;
    if (delta > 180) {
        delta = 360 - delta;
    } else if (delta < -180) {
        delta = 360 + delta;
    }
    delta = fabs(delta);
    if (delta >= range) return 0;
    return 1 - delta / range;
}

// 黑金色转换
uchar4 RS_KERNEL blackGold(uchar4 in) {
    float4 f4 = rsUnpackColor8888(in);
    float3 result;
    float Cmax = max(f4.r, max(f4.g, f4.b));
    float Cmin = min(f4.r, min(f4.g, f4.b));
    float delta = Cmax - Cmin;
    float H = 0;
    float S = Cmax == 0 ? 0 : (delta / Cmax);
    float V = Cmax;
    if (delta != 0) {
        if (Cmax == f4.r) {
            H = 60 * fmod((f4.g - f4.b) / delta + 6, 6);
        } else if (Cmax == f4.g) {
            H = 60 * ((f4.b - f4.r) / delta + 2);
        } else if (Cmax == f4.b) {
            H = 60 * ((f4.r - f4.g) / delta + 4);
        }
    }

    if (H >= 65 && H <= 345) {
        result = dot(f4.rgb, grayWeight);
        return rsPackColorTo8888(result);
    }

    if (H > 50 && H < 65) {
        float weight = calWeight(H, 65, 15);
        H = (H - 50) * 0.2f * weight + 50;
    }

    if (H < 25 || H > 345) {
        float base = H < 25 ? 25 : 25 + 360;
        float weight = calWeight(H, 0, 25);
        H = fmod(((base - H) * 0.4f * weight + H), 360);
    }

    float C = V * S;
    float X = C * (1 - fabs(fmod(H / 60, 2) - 1));
    float m = V - C;
    if (H >= 0 && H < 60) {
        result.r = C;
        result.g = X;
        result.b = 0;
    } else if (H >= 60 && H < 120) {
        result.r = X;
        result.g = C;
        result.b = 0;
    } else if (H >= 120 && H < 180) {
        result.r = 0;
        result.g = C;
        result.b = X;
    } else if (H >= 180 && H < 240) {
        result.r = 0;
        result.g = X;
        result.b = C;
    } else if (H >= 240 && H < 300) {
        result.r = X;
        result.g = 0;
        result.b = C;
    } else if (H >= 300 && H < 360) {
        result.r = C;
        result.g = 0;
        result.b = X;
    }
    result.r += m;
    result.g += m;
    result.b += m;

    if (H > 20 && H < 60) {
        float weight = calWeight(H, 20, 20);
        float3 gray = dot(result, grayWeight);
        result = mix(gray, result, 0.3f * weight + 1);
    }

    return rsPackColorTo8888(result);
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
    uchar4 out = in;
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
    uchar4 out = in;
    out.r = R < 255 ? R : 255;
    out.g = G < 255 ? G : 255;
    out.b = B < 255 ? B : 255;
    return out;
}
