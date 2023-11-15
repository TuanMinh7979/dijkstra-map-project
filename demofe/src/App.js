import React, { useEffect, useState } from "react";

import { Marker, Polyline } from "react-leaflet";
import { deleteAPI, getAPI } from "./fetchData";

import Map from "./Map";
import "leaflet/dist/leaflet.css";
import "./App.css";

import { greenIcon, blueIcon } from "./components/leaftlet/Icon";
import SearchBar from "./components/searchbar/SearchBar";
import SelectDropDown from "./components/dropdown/SelectDropDown"
import Dialog from "./components/dialog/Dialog";


import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import Loading from "./components/loading/Loading";
import { API_URL } from "./constant";
function App() {


  const [center, setCenter] = useState({ lat: "", lng: "" });
  const [path, setPath] = useState([]);
  const [activeMap, setActiveMap] = useState("");
  const [mapList, setMapList] = useState([]);
  const [places, setPlaces] = useState([]);
  const [loading, setLoading] = useState(false);



  const fetchMaps = async () => {
    const rsdata = await getAPI(`${API_URL}/maps`)
    setMapList(rsdata);
    setActiveMap(rsdata[0]);
  };

  const fetchCurMap = async () => {
    if (activeMap) {
      const centerPlace = await getAPI(`${API_URL}/map/${activeMap}`);

      setCenter({ lng: centerPlace.lng, lat: centerPlace.lat });
      const allPlaces = await getAPI(`${API_URL}/places`);

      setPlaces([...allPlaces]);
    }
  };

  useEffect(() => {
    fetchMaps();
  }, []);
  useEffect(() => {
    fetchCurMap();

  }, [activeMap]);

  const [selectedFile, setSelectedFile] = useState(null);
  const onChangeFile = (e) => {
    setSelectedFile(e.target.files[0]);
  };
  const uploadFile = async () => {
    if (selectedFile) {
      try {
        setLoading(true)
        const formData = new FormData();

        formData.append("file", selectedFile);

        const rs = await fetch(`${API_URL}/add-new-map`, {
          method: "POST",
          body: formData,
        });
        const rsdata = await rs.json();


        setSelectedFile(null)
        setLoading(false)
        setMapList([...mapList, rsdata.filename])
      }
      catch (e) {
        console.log(e);
        setLoading(false)
      }

    }
  };

  const searchSubmit = async () => {
    if (!findData.from || !findData.to) {
      return;
    }
    const resdata = await fetch(
      `${API_URL}/search?source=${findData.from}&destination=${findData.to}`
    )
      .then((response) => {
        if (!response.ok) {
          throw new Error("Error");
        }

        return response.json();
      })

      .catch((error) => {
        console.error(error);
      });

    setPath(resdata);
  };



  const [findData, setFindData] = useState({ from: "", to: "" });

  const choosePlace = (placeId, name) => {
    setFindData({
      ...findData,
      [name]: placeId,
    });
  };

  const [dialogData, setDialogData] = useState({
    isOpen: false,
    data: ''
  })



  const handleClickDelete = (toDeleteMapName) => {
    setDialogData({
      isOpen: true,
      data: toDeleteMapName
    })
  }


  const deleteService = async () => {
    if (dialogData.data) {
      const rs = await deleteAPI(`${API_URL}/map/${dialogData.data}`);
      toast.info("Delete success")
      setMapList([...mapList].filter(el => el != dialogData.data))
    }
  }

  return (
    <div>
      {loading && <Loading></Loading>}
      <ToastContainer />
      {
        dialogData.isOpen && <Dialog
          title={`Wanna delete map ${dialogData.data}?`}
          showButtons={true}
          firstButtonText="OK"
          secondButtonText="CANCEL"
          firstBtnHandler={() => {
            deleteService()
            setDialogData({ isOpen: false, data: '' })
          }}
          secondBtnHandler={() => {
            setDialogData({ isOpen: false, data: '' })
          }} />
      }

      <div className="header" >
        <div className="left" >
          <SearchBar
            name="from"
            choosePlace={choosePlace}
            data={places}

          ></SearchBar>

          <SearchBar
            name="to"
            choosePlace={choosePlace}
            data={places}
          ></SearchBar>

          <button className="findBtn" onClick={searchSubmit}>Find</button>
        </div>

        <div className="right" >
          <SelectDropDown classNameProp="maps-dropdown" value={activeMap} data={mapList} handleOnClick={(el) => setActiveMap(el)}
            handleClickDelete={handleClickDelete} />
          <div className="upload">
            <label htmlFor="">New map</label>
            <input onChange={onChangeFile} type="file"  />
            <button disabled={!selectedFile} onClick={uploadFile}>Upload</button>
          </div>

        </div>
      </div>
      {center.lat && center.lng && (
        <Map
          styles={{ height: "500px", width: "80%", margin: "100px auto" }}
          center={[center.lat, center.lng]}
          zoom={16}
        >
          {path.length > 0 && (
            <>
              <Marker position={[path[0].lat, path[0].lon]} icon={blueIcon} />
              <Marker
                position={[
                  path[path.length - 1].lat,
                  path[path.length - 1].lon,
                ]}
                icon={greenIcon}
              />
              <Polyline
                positions={path}
                pathOptions={{
                  color: "blue",
                  weight: 6,
                }}
              />
            </>
          )}
        </Map>
      )}
    </div>
  );
}

export default App;
