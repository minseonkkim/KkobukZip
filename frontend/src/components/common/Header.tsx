import { useState, lazy, Suspense, memo } from "react";
import useDeviceStore from "../../store/useDeviceStore";
import { useWeb3Store } from "../../store/useWeb3Store";
import usePriorityLoading from "../../hooks/usePriorityLoading";
import LogoImg from "../../assets/logo.webp";
import CoinImg from "../../assets/Coin.webp";
import MyPageImg from "../../assets/mypage.webp";
import { Link, useLocation, useNavigate } from "react-router-dom";
import Modal from "./Modal";
import { useUserStore } from "../../store/useUserStore";
import { RiLogoutBoxLine } from "@react-icons/all-files/ri/RiLogoutBoxLine";
import { logoutRequest } from "../../apis/userApi";
import { useStore } from "zustand";

const Wallet = lazy(() => import("./Wallet"));

const Logo = memo(() => (
  <div className="text-[32px] lg:text-[38px] font-dnf-bitbit flex flex-row items-center cursor-pointer">
    <img
      src={LogoImg}
      className="w-[50px] lg:w-[60px] h-[50px] lg:h-[60px] mr-3 object-contain"
      draggable="false"
      alt="Logo Image"
    />
    <div className="whitespace-nowrap">
      <span className="text-[#4B721F]">꼬북</span>ZIP
    </div>
  </div>
));

export default function Header() {
  const isMobile = useDeviceStore((state) => state.isMobile);
  const { isLogin, userInfo, setLogout } = useUserStore();
  const { account } = useWeb3Store();
  const [isWalletOpen, setIsWalletOpen] = useState(false);
  const location = useLocation();
  const shouldLoadWallet = usePriorityLoading(1);
  const navigate = useNavigate();
  const headerBackgroundColor = location.pathname === "/" ? "#AAE0F2" : "#fff";
  const role = useStore(useUserStore, (state) => state.userInfo?.role);
  const toggleWallet = () => {
    setIsWalletOpen((prev) => !prev);
  };

  const handleLogout = () => {
    logoutRequest();
    setLogout();
    navigate("/");
  };

  return (
    <header>
      <nav
        className="fixed top-0 left-0 w-full h-[62px] lg:h-[75px] px-4 lg:px-[250px] flex flex-row justify-between items-center shadow-md z-50"
        style={{ backgroundColor: headerBackgroundColor }}
      >
        <Link to="/">
          <Logo />
        </Link>

        <div className="flex flex-row items-center">
          {!isMobile && isLogin && (
            <>
              <div className="mr-3 font-bold text-[22px] cursor-pointer font-stardust xl:block hidden">
                {userInfo?.nickname}님 로그인 중
              </div>
              <div
                onClick={handleLogout}
                className="mr-3 text-[24px] cursor-pointer"
              >
                <RiLogoutBoxLine />
              </div>
            </>
          )}
          {!isLogin ? (
            <div className="flex flex-row space-x-6 items-center font-bold">
              <Link to="/login">
                <div className="cursor-pointer font-stardust">
                  <span className="whitespace-nowrap text-[20px] lg:text-[23px] tracking-widest">
                    로그인
                  </span>
                </div>
              </Link>
              <Link to="/join">
                <div className="cursor-pointer font-stardust">
                  <span className="whitespace-nowrap text-[20px] lg:text-[23px] tracking-widest">
                    회원가입
                  </span>
                </div>
              </Link>
            </div>
          ) : (
            <>
              <div
                className={`${
                  isMobile ? "rounded-full px-1.5" : "rounded-[10px] px-2"
                } py-1.5 mr-3 bg-[#F6CA19] hover:bg-[#DFB509] shadow-[3px_3px_0px_#C49B07] hover:shadow-[3px_3px_0px_#CAA612] flex flex-row items-center gap-1 cursor-pointer font-dnf-bitbit active:scale-95 ${account === "" && "motion-safe:animate-pulse"}`}
                onClick={toggleWallet}
              >
                <img
                  src={CoinImg}
                  className="w-[27px] h-[27px]"
                  draggable="false"
                  alt="Coin Image"
                  loading="lazy"
                />
                {!isMobile && (
                  <span className="whitespace-nowrap text-white text-[17px] lg:text-[20px] tracking-widest">
                    내 지갑
                  </span>
                )}
              </div>
              <Link
                to={role === "ROLE_ADMIN" ? "/admin/document/list" : "/mypage"}
              >
                <div
                  className={`${
                    isMobile ? "rounded-full px-1.5" : "rounded-[10px] px-2"
                  } py-1.5 hover:shadow-[3px_3px_0px_#8E70D3] shadow-[3px_3px_0px_#8568CB] hover:bg-[#9B8BC1] bg-[#B9A6E6] flex flex-row items-center cursor-pointer font-dnf-bitbit active:scale-95`}
                >
                  {isMobile ? (
                    <img
                      src={MyPageImg}
                      className="whitespace-nowrap w-[25px] h-[25px]"
                      draggable="false"
                      alt="My Page Image"
                      loading="lazy"
                    />
                  ) : (
                    <span className="whitespace-nowrap text-white text-[17px] lg:text-[20px] tracking-widest">
                      {role !== "ROLE_ADMIN" ? "마이페이지" : "문서 관리"}
                    </span>
                  )}
                </div>
              </Link>
            </>
          )}
        </div>

        {shouldLoadWallet && (
          <Suspense fallback={null}>
            <Modal isOpen={isWalletOpen} onClose={toggleWallet}>
              <Wallet />
            </Modal>
          </Suspense>
        )}
      </nav>
    </header>
  );
}