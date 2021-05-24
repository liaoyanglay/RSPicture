#pragma version(1)
#pragma rs java_package_name(com.dizzylay.rspicture.rs)
#pragma rs_fp_relaxed

rs_allocation inputAllocation;

int radius = 0;
float weight = 0;  // 0-260f
uchar4 result = 0;

uchar4 RS_KERNEL magnify(uchar4 in, int x, int y) {
    int total=(2*radius+1)*(2*radius+1);
    float Rdenominator=0;
    float Gdenominator=0;
    float Bdenominator=0;
    float Adenominator=0;
    float Rmolecular=0;
    float Gmolecular=0;
    float Bmolecular=0;
    float Amolecular=0;
    int dx=-radius;
    int dy=-radius-1;

    uchar4 cur= rsGetElementAt_uchar4(inputAllocation, x, y);

    for(int i=0;i<total;i++){
        if(i%(2*radius+1)==0){
            dx=-radius;
            dy++;
        }

        uchar4 sam= rsGetElementAt_uchar4(inputAllocation, x+dx, y+dy);

        float rRatio=fmax((float)0,1-abs(sam.r-cur.r)/weight);
        float gRatio=fmax((float)0,1-abs(sam.g-cur.g)/weight);
        float bRatio=fmax((float)0,1-abs(sam.b-cur.b)/weight);
        float aRatio=fmax((float)0,1-abs(sam.a-cur.a)/weight);

        Rmolecular+=(rRatio*sam.r);
        Gmolecular+=(gRatio*sam.g);
        Bmolecular+=(bRatio*sam.b);
        Amolecular+=(aRatio*sam.a);

        Rdenominator+=rRatio;
        Gdenominator+=gRatio;
        Bdenominator+=bRatio;
        Adenominator+=aRatio;

        dx++;
    }

    result.r=min((float)255,Rmolecular/Rdenominator);
    result.g=min((float)255,Gmolecular/Gdenominator);
    result.b=min((float)255,Bmolecular/Bdenominator);
    result.a=min((float)255,Amolecular/Adenominator);
    return result;
}
