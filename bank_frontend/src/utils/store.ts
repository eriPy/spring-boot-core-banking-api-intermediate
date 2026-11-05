import { create } from "zustand";

interface AccountStore {
    accountType: string;
    personType: string;
    setAccountType: (type: string) => void;
    setPersonType: (type: string) => void;
    clearStore: () => void;
}

export const useAccountStore = create<AccountStore>((set) => ({
    accountType: "",
    personType: "",
    setAccountType: (type) => set({ accountType: type }),
    setPersonType: (type) => set({ personType: type }),
    clearStore: () => set({ accountType: "", personType: "" }),
}));

interface UserStore {
    token: string;
    accountHolder: string;
    email: string;
    personType: string;
    monthlyEarnings: number;
    accountTypes: string[];
    accountCard: string[];
    setToken: (type: string) => void;
    setAccountHolder: (type: string) => void;
    setEmail: (type: string) => void;
    setPersonType: (type: string) => void;
    setMonthlyEarnings: (type: number) => void;
    setAccountTypes: (type: string[]) => void;
    setAccountCard: (type: string[]) => void;
}

export const useUserStore = create<UserStore>((set) => ({
    token: "",
    accountHolder: "",
    email: "",
    personType: "",
    monthlyEarnings: 0,
    accountTypes: [],
    accountCard: [],
    setToken: (type) => set({token: type}),
    setAccountHolder: (type) => set({ accountHolder: type }),
    setEmail: (type) => set({ email: type }),
    setPersonType: (type) => set({ personType: type }),
    setMonthlyEarnings: (type) => set({ monthlyEarnings: type }),
    setAccountTypes: (type) => set({ accountTypes: type }),
    setAccountCard: (type) => set({ accountCard: type }),
}));