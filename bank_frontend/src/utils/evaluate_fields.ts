import type { Dispatch, SetStateAction } from "react";

export const evaluateString = (...fields: string[]) => fields.some(field => !field || !field.trim());

export const resetStrings = (...fields: Dispatch<SetStateAction<string>>[]) => fields.forEach((field) => field(""));

export const resetNumber = (...fields: Dispatch<SetStateAction<number>>[]) => fields.forEach((field) => field(0.01));

export const evaluateIdCard = (idCard: string) => {
    if (idCard.length != 10) return false;
    let total = 0;
    let startIn = 9;
    for (let i = 0; i < idCard.length; i++) {
        if (i === 8 && idCard.charAt(8) === "-") continue;
        if (i !== 8 && /^\d$/.test(idCard.charAt(i))) {
            if (i == 9) continue;
            const value: number = Number(idCard.charAt(i));
            total +=value * startIn;
            startIn--;
            continue;
        }
        return false;
    }
    const lastDigit: number = Number(idCard.charAt(9));
    const residue = total % 10;
    return (residue === 0 && residue === lastDigit) || Math.abs(10 - residue) === lastDigit;
}