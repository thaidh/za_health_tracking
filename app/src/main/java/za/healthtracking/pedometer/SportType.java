package za.healthtracking.pedometer;

import za.healthtracking.utils.ComputationUtil;

/**
 * Created by hiepmt on 10/07/2017.
 */

public class SportType {
    public static double a(int type, double speedInMiles, float f, boolean weight) {
        double result;
        switch (type) {
            case 1:
            case 14:
                result = (float) ((((0.0024d * speedInMiles) * speedInMiles) - (0.0104d * speedInMiles)) + 0.1408d);
                break;
            case 3:
            case 15:
                result = (float) (Math.exp(0.125d * speedInMiles) * 0.0207d);
                break;
            case 4:
                result = (float) (0.0216d * Math.exp(0.144d * speedInMiles));
                break;
            case 6:
            case 54:
                result = (float) ((0.0116734d * speedInMiles) + 0.04d);
                break;
            case 7:
                result = 0.07f;
                break;
            case 8:
            case 53:
            case 60:
                result = (float) ((0.0153d * speedInMiles) + 0.0619d);
                break;
            case 9:
            case 10:
            case 43:
            case 61:
                result = 0.106f;
                break;
            case 11:
                if (!weight) {
                    result = 0.015000029f;
                    break;
                }
                result = 0.0166667f;
                break;
            case 13:
                result = 0.166f;
                break;
            case 16:
                result = (((((0.0024d * speedInMiles) * speedInMiles) - (0.0104d * speedInMiles)) + 0.1408d) * 0.9d);
                break;
            case 17:
                result = ((0.0329d * speedInMiles) + 0.0338d);
                break;
            case 18:
                result = ComputationUtil.convertMileToKilometer((float) speedInMiles) / 3.6f;
                if (result <= 0.0f) {
                    result = 0.232f;
                    break;
                }
                result = ((result * 65.6168f) * 0.0037f) - 6.0E-4f;
                break;
            case 19:
            case 58:
                return 0.06044439971446991d;
            case 20:
                return 0.1368888020515442d;
            case 21:
                return 0.08622200042009354d;
            case 22:
                result = (float) (0.0201d * Math.exp(0.107d * speedInMiles));
                break;
            case 23:
            case 24:
                result = 0.1151f;
                break;
            case 25:
                result = 0.152f;
                break;
            case 26:
                result = 0.0578f;
                break;
            case 27:
                result = 0.1154f;
                break;
            case 28:
                result = 0.108f;
                break;
            case 29:
                result = 0.0467f;
                break;
            case 30:
                result = 0.0507f;
                break;
            case 31:
                result = 0.0542f;
                break;
            case 32:
                result = 0.183f;
                break;
            case 33:
                result = 0.053f;
                break;
            case 34:
                result = 0.0926f;
                break;
            case 35:
                result = 0.0588f;
                break;
            case 36:
                result = (Math.exp(0.125d * speedInMiles) * 0.0207d);
                break;
            case 37:
                result = ((0.0153d * speedInMiles) + 0.0619d);
                break;
            case 38:
                result = 0.1402f;
                break;
            case 42:
                result = 0.0507f;
                break;
            case 44:
                result = 0.0992f;
                break;
            case 45:
                result = 0.194f;
                break;
            case 46:
                result = 0.1168f;
                break;
            case 47:
                result = 0.0467f;
                break;
            case 48:
                result = 0.1058f;
                break;
            case 50:
                result = 0.15067f;
                break;
            case 51:
                result = 0.070667f;
                break;
            case 52:
                result = 0.1719f;
                break;
            case 55:
                result = 0.1168f;
                break;
            case 67:
                result = 0.1342f;
                break;
            case 68:
                result = 0.08598f;
                break;
            case 69:
                result = 0.0926f;
                break;
            case 70:
                result = 0.08378f;
                break;
            case 71:
                result = 0.11464f;
                break;
            case 72:
                result = 0.084f;
                break;
            case 73:
                result = 0.08378f;
                break;
            case 74:
                result = 0.06667f;
                break;
            case 75:
                result = 0.16755f;
                break;
            case 76:
                result = 0.0992f;
                break;
            default:
                result = ((((0.008d * speedInMiles) * speedInMiles) - (0.0301d * speedInMiles)) + 0.0822d);
                break;
        }
        return result;
    }
}
