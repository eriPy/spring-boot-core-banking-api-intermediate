import type { Dispatch, SetStateAction } from "react";

export const showAdv = (setter: Dispatch<SetStateAction<boolean>>) => {
    setter(true);
    setTimeout(() => setter(false), 3000);
}

export const conditionAdv = (condition: boolean, setter: Dispatch<SetStateAction<boolean>>) => {
    if (condition) {
        showAdv(setter);
        return true;
    }
    return false;
}