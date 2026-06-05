import DashboardLayout from "../layout/DashboardLayout.jsx";
import {useEffect, useState} from "react";
import { File, FileIcon, FileText, Grid, Image, List, Music, Video } from "lucide-react";
import axios from "axios";
import toast from "react-hot-toast";
import {useNavigate} from "react-router-dom";
import FileCard from "../components/FileCard.jsx";
import {apiEndpoints} from "../util/apiEndpoints.js";
import ConfirmationDialog from "../components/ConfirmationDialog.jsx";

import FileListRow from "../components/FileListRow.jsx";

const MyFiles = () => {
    const [files, setFiles] = useState([]);
    const [loadError, setLoadError] = useState("");
    const [viewMode, setViewMode] = useState("list");
    const navigate = useNavigate();
    const [deleteConfirmation, setDeleteConfirmation] = useState({
        isOpen: false,
        fileId: null
    });

    const getPublicFileLink = (fileId) => `${window.location.origin}/public/${fileId}`;

    //fetching the files for a logged in user
    const fetchFiles = async () => {
        try {
            const response = await axios.get(apiEndpoints.FETCH_FILES);
            if (response.status === 200) {
                const normalizedFiles = response.data.map((file) => ({
                    ...file,
                    fileName: file.fileName || file.name || "Unnamed file",
                    url: file.url || file.fileLocation || ""
                }));
                setFiles(normalizedFiles);
                setLoadError("");
            }
        }catch (error) {
            setFiles([]);
            setLoadError("Unable to load data");
            toast.error('Unable to load files right now.');
        }
    }

    //Toggles the public/private status of a file
    const togglePublic = async (fileToUpdate) => {
        try {
            const res = await axios.patch(apiEndpoints.TOGGLE_FILE(fileToUpdate.id), {});
            const updated = res.data;
            setFiles(files.map((file) => file.id === fileToUpdate.id ? {...file, ...updated} : file));
        }catch (error) {
            toast.error('Unable to update sharing status.');
        }
    }

    //Handle file download
    const handleDownload = async (file) => {
        if (!file?.id) {
            toast.error('Unable to download file right now.');
            return;
        }

        try {
            const extension = (file.fileName || file.name || '').split('.').pop().toLowerCase();

            // For document-like types, use server-streamed download to avoid Cloudinary ACL issues
            const docLike = ['pdf','doc','docx','xls','xlsx','ppt','pptx','txt','rtf','csv'];
            if (docLike.includes(extension) || (file.type && !file.type.startsWith('image/'))) {
                const downloadRes = await axios.get(apiEndpoints.DOWNLOAD_FILE(file.id), { responseType: 'blob' });
                // try to get filename from Content-Disposition header
                const disposition = downloadRes.headers['content-disposition'] || downloadRes.headers['Content-Disposition'];
                let filename = file.fileName || file.name || 'download';
                if (disposition) {
                    const match = /filename\*=UTF-8''([^;\n\r]+)|filename="?([^";]+)"?/.exec(disposition);
                    if (match) {
                        filename = decodeURIComponent(match[1] || match[2]);
                    }
                }
                const blobUrl = window.URL.createObjectURL(new Blob([downloadRes.data]));
                const link = document.createElement('a');
                link.href = blobUrl;
                link.setAttribute('download', filename);
                document.body.appendChild(link);
                link.click();
                link.remove();
                window.URL.revokeObjectURL(blobUrl);
                return;
            }

            // Ask backend for a signed Cloudinary URL and redirect browser to it for images
            const res = await axios.get(apiEndpoints.SIGNED_URL(file.id));
            const url = res.data?.url;
            if (url) {
                window.location.href = url;
                return;
            }

            // fallback to server-streamed download
            const downloadRes = await axios.get(apiEndpoints.DOWNLOAD_FILE(file.id), { responseType: 'blob' });
            const blobUrl = window.URL.createObjectURL(new Blob([downloadRes.data]));
            const link = document.createElement('a');
            link.href = blobUrl;
            link.setAttribute('download', file.fileName || file.name || 'download');
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(blobUrl);
        } catch (error) {
            toast.error('Unable to download file right now.');
        }
    }

    const handleViewFile = (file) => {
        if (!file?.id) {
            toast.error("Unable to load data");
            return;
        }

        if (file.isPublic) {
            navigate(`/public/${file.id}`);
            return;
        }

        if (file?.id) {
            window.open(apiEndpoints.VIEW_FILE(file.id), "_blank", "noopener,noreferrer");
            return;
        }

        toast.error("Unable to load data");
    };

    const handleShareLink = async (file) => {
        if (!file?.isPublic || !file?.id) {
            toast.error("Make the file public before sharing it.");
            return;
        }

        try {
            await navigator.clipboard.writeText(getPublicFileLink(file.id));
            toast.success("Link copied!");
        } catch (error) {
            toast.error("Unable to copy link");
        }
    };

    //Closes the delete confirmation modal
    const closeDeleteConfirmation = () => {
        setDeleteConfirmation({
            isOpen: false,
            fileId: null
        })
    }

    //Opens the delete confirmation modal
    const openDeleteConfirmation = (fileId) => {
        setDeleteConfirmation({
            isOpen: true,
            fileId
        })
    }

    //Delete a file after confirmation
    const handleDelete = async () => {
        const fileId = deleteConfirmation.fileId;
        if (!fileId) return;

        try {
            const response = await axios.delete(apiEndpoints.DELETE_FILE(fileId));
            if (response.status === 204) {
                setFiles(files.filter((file) => file.id !== fileId));
                closeDeleteConfirmation();
            } else {
                toast.error('Error deleting file');
            }
        }catch (error) {
            toast.error('Unable to delete file right now.');
        }
    }

    useEffect(() => {
        fetchFiles();
    }, []);

    const getFileIcon = (file) => {
        const extenstion = (file.fileName || file.name || "").split('.').pop().toLowerCase();

        if (['jpg', 'jpeg', 'png', 'gif', 'svg', 'webp'].includes(extenstion)) {
            return <Image size={24} className="text-purple-500" />
        }

        if (['mp4', 'webm', 'mov', 'avi', 'mkv'].includes(extenstion)) {
            return <Video size={24} className="text-blue-500" />
        }

        if (['mp3', 'wav', 'ogg', 'flac', 'm4a'].includes(extenstion)) {
            return <Music size={24} className="text-green-500" />
        }

        if (['pdf', 'doc', 'docx', 'txt', 'rtf'].includes(extenstion)) {
            return <FileText size={24} className="text-amber-500" />
        }

        return <FileIcon size={24} className="text-purple-500" />
    }

    return (
        <DashboardLayout activeMenu="My Files">
            <div className="p-6">
                <div className="flex justify-between items-center mb-6">
                    <h2 className="text-2xl font-bold">My Files {files.length}</h2>
                    <div className="flex items-center gap-3">
                        <List
                            onClick={() => setViewMode("list")}
                            size={24}
                            className={`cursor-pointer transition-colors ${viewMode === 'list' ? 'text-blue-600': 'text-gray-400 hover:text-gray-600'}`} />
                        <Grid
                            size={24}
                            onClick={() => setViewMode("grid")}
                            className={`cursor-pointer transition-colors ${viewMode === 'grid' ? 'text-blue-600': 'text-gray-400 hover:text-gray-600'}`} />
                    </div>
                </div>

                {loadError && (
                    <div className="mb-4 bg-red-50 text-red-700 rounded-lg p-4">{loadError}</div>
                )}

                {files.length === 0 ? (
                    <div className="bg-white rounded-lg shadow p-12 flex flex-col items-center justify-center">
                        <File
                            size={60}
                            className="text-purple-300 mb-4"
                        />
                        <h3 className="text-xl font-medium text-gray-700 mb-2">
                            No files uploaded yet
                        </h3>
                        <p className="text-gray-500 text-center max-w-md mb-6">
                            Start uploading files to see them listed here. you can upload
                            documents, images, and other files to share and manage them securely.
                        </p>
                        <button
                            onClick={() => navigate('/upload')}
                            className="px-4 py-2 bg-purple-500 text-white rounded-md hover:bg-purple-600 transition-colors">
                            Go to Upload
                        </button>
                    </div>
                ): viewMode === "grid" ? (
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                        {files.map((file) => (
                            <FileCard
                                key={file.id}
                                file={file}
                                onDelete={openDeleteConfirmation}
                                onTogglePublic={togglePublic}
                                onDownload={handleDownload}
                                onShareLink={handleShareLink}
                                onViewFile={handleViewFile}
                            />
                        ))}
                    </div>
                ) : (
                    <div className="overflow-x-auto bg-white rounded-lg shadow">
                        <table className="min-w-full">
                            <thead className="bg-gray-50 border-b border-gray-200">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Size</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Uploaded</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Sharing</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-200">
                                {files.map((file) => (
                                    <FileListRow
                                        key={file.id}
                                        file={file}
                                        onDownload={handleDownload}
                                        onDelete={openDeleteConfirmation}
                                        onTogglePublic={togglePublic}
                                        onShareLink={handleShareLink}
                                        onViewFile={handleViewFile}
                                        getFileIcon={getFileIcon}
                                    />
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
                {/* Delete confiramtion dialog*/}
                <ConfirmationDialog
                    isOpen={deleteConfirmation.isOpen}
                    onClose={closeDeleteConfirmation}
                    title="Delete File"
                    message="Are you sure want to delete this file? This action cannot be undone."
                    confirmText="Delete"
                    cancelText="Cancel"
                    onConfirm={handleDelete}
                    confirmButtonClass="bg-red-600 hover:bg-red-700"
                />
            </div>
        </DashboardLayout>
    )
}

export default MyFiles;