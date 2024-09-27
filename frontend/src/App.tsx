import { BrowserRouter, Route, Routes } from "react-router-dom";
import "./App.css";
import { HelmetProvider } from "react-helmet-async";
import React, { Suspense } from "react";
import LoadingImage from "../src/assets/loading.gif";

const MainPage = React.lazy(() => import("./pages/common/MainPage"));
const LoginPage = React.lazy(() => import("./pages/common/LoginPage"));
const JoinPage = React.lazy(() => import("./pages/common/JoinPage"));
const DocumentFormPage = React.lazy(() =>
  import("./pages/document/DocumentFormPage")
);
const DocumentListPage = React.lazy(() =>
  import("./pages/document/DocumentListPage")
);
const TransactionDetailPage = React.lazy(() =>
  import("./pages/transaction/TransactionDetailPage")
);
const TransactionListPage = React.lazy(() =>
  import("./pages/transaction/TransactionListPage")
);
const AuctionDetailPage = React.lazy(() =>
  import("./pages/auction/AuctionDetailPage")
);
const AuctionListPage = React.lazy(() =>
  import("./pages/auction/AuctionListPage")
);
const MyPage = React.lazy(() => import("./pages/user/MyPage"));
const AdminDocsListPage = React.lazy(() =>
  import("./pages/user/admin/AdminDocsListPage")
);
const AdminDocsDetailPage = React.lazy(() =>
  import("./pages/user/admin/AdminDocsDetailPage")
);
const ChatList = React.lazy(() => import("./components/chatting/ChatList"));
const BreedDocument = React.lazy(() =>
  import("./components/document/BreedDocument")
);
const AssignDocument = React.lazy(() =>
  import("./components/document/AssignDocument")
);
const GrantorDocument = React.lazy(() =>
  import("./components/document/GrantorDocument")
);
const DeathDocument = React.lazy(() =>
  import("./components/document/DeathDocument")
);
const AuctionSuccessPage = React.lazy(() =>
  import("./pages/auction/AuctionSuccessPage")
);
const AuctionRegisterPage = React.lazy(() =>
  import("./pages/auction/AuctionRegisterPage")
);
const NotFoundPage = React.lazy(() => import("./pages/common/NotFoundPage"));
const TransactionRegisterPage = React.lazy(() =>
  import("./pages/transaction/TransactionRegisterPage")
);

function App() {
  return (
    <HelmetProvider>
      <BrowserRouter>
        <Suspense
          fallback={
            <div className="w-full h-[100vh] flex flex-col justify-center items-center">
              <img
                src={LoadingImage}
                className="loading-image"
                alt="Loading"
                draggable="false"
              />
              <p className="typing-effect font-stardust font-bold text-[30px]">Loading...</p>
            </div>
          }
        >
          <Routes>
            {/* Common Domain */}
            <Route path="/" element={<MainPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/join" element={<JoinPage />} />

            {/* Document Domain */}
            <Route path="/doc-form" element={<DocumentFormPage />}>
              <Route path="/doc-form/breed" element={<BreedDocument />} />
              <Route path="/doc-form/assign" element={<AssignDocument />} />
              <Route path="/doc-form/grant" element={<GrantorDocument />} />
              <Route path="/doc-form/death" element={<DeathDocument />} />
            </Route>
            <Route path="/doc-list" element={<DocumentListPage />} />

            {/* Auction Domain - 경매 */}
            <Route
              path="/auction-detail/:auctionId"
              element={<AuctionDetailPage />}
            />
            <Route path="/auction-list" element={<AuctionListPage />} />
            <Route path="/auction-success" element={<AuctionSuccessPage />} />
            <Route path="/auction-register" element={<AuctionRegisterPage />} />

            {/* Transaction Domain - 거래 */}
            <Route
              path="/transaction-detail"
              element={<TransactionDetailPage />}
            />
            <Route path="/transaction-list" element={<TransactionListPage />} />
            <Route
              path="/transaction-register"
              element={<TransactionRegisterPage />}
            />

            {/* User Domain - 유저 */}
            <Route path="/mypage" element={<MyPage />} />

            {/* Admin */}
            <Route
              path="/admin/document/list"
              element={<AdminDocsListPage />}
            />
            <Route
              path="/admin/:turtleUUID/:documentHash"
              element={<AdminDocsDetailPage />}
            />

            {/* Not Found */}
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
          <ChatList />
        </Suspense>
      </BrowserRouter>
    </HelmetProvider>
  );
}

export default App;
